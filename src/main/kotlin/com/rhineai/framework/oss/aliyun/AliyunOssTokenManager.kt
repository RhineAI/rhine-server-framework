package com.rhineai.framework.oss.aliyun

import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.sts20150401.AsyncClient
import com.aliyun.sdk.service.sts20150401.models.AssumeRoleRequest
import com.aliyun.sdk.service.sts20150401.models.AssumeRoleResponse
import com.rhineai.framework.constant.CommonErrorCode
import com.rhineai.framework.entity.vo.alioss.TokenStatus
import com.rhineai.framework.exception.BusinessException
import com.rhineai.framework.util.RedisUtil
import darabonba.core.client.ClientOverrideConfiguration
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class AliyunOssTokenManager(
    private val redisUtil: RedisUtil,
    private val properties: AliyunOssProperties
) {
    private val logger = LoggerFactory.getLogger(AliyunOssTokenManager::class.java)

    fun getAuthToken() {
        val provider = StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId(properties.accessKeyId)
                .accessKeySecret(properties.accessKeySecret)
                .build()
        )
        val client: AsyncClient = AsyncClient.builder()
            .region(properties.region)
            .credentialsProvider(provider)
            .overrideConfiguration(
                ClientOverrideConfiguration.create()
                    .setEndpointOverride(properties.stsEndpoint)
                    .setConnectTimeout(Duration.ofSeconds(43200))
            )
            .build()
        val assumeRoleRequest: AssumeRoleRequest =
            AssumeRoleRequest.builder()
                .roleArn(properties.role.arn)
                .roleSessionName(properties.role.sessionName)
                .build()
        val response: CompletableFuture<AssumeRoleResponse> = client.assumeRole(assumeRoleRequest)
        val resp: AssumeRoleResponse = response.get()
        val expirationTime: Long = DateUtil.between(
            DateUtil.parseDate(DateUtil.now()),
            DateUtil.parseDate(resp.body.credentials.expiration),
            DateUnit.MS
        )
        redisUtil.setEx(properties.redisTokenKey, resp.body.credentials.securityToken, expirationTime, TimeUnit.MICROSECONDS)
        redisUtil.setEx(properties.redisAccessKeySecretKey, resp.body.credentials.accessKeySecret, expirationTime, TimeUnit.MICROSECONDS)
        redisUtil.setEx(properties.redisAccessKeyId, resp.body.credentials.accessKeyId, expirationTime, TimeUnit.MICROSECONDS)
        client.close()
    }

    fun getToken(): Map<String, String> {
        val lockAcquired = tryAcquireLock()
        return try {
            val currentToken = checkExistingToken()
            when {
                currentToken.missing || currentToken.expiring -> {
                    if (lockAcquired) {
                        refreshToken()
                        val newToken = checkExistingToken()
                        return newToken.toMap()
                    } else {
                        waitAndRetry()
                    }
                }
                else -> currentToken.toMap()
            }
        } finally {
            if (lockAcquired) {
                releaseLock()
            }
        }
    }

    private fun refreshToken(maxRetries: Int = 3) {
        var retryCount = 0
        while (retryCount < maxRetries) {
            try {
                val provider = StaticCredentialProvider.create(
                    Credential.builder()
                        .accessKeyId(properties.accessKeyId)
                        .accessKeySecret(properties.accessKeySecret)
                        .build()
                )

                val client = AsyncClient.builder()
                    .region(properties.region)
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                            .setEndpointOverride(properties.stsEndpoint)
                    )
                    .build()

                val request = AssumeRoleRequest.builder()
                    .roleArn(properties.role.arn)
                    .roleSessionName(properties.role.sessionName)
                    .build()

                val response: CompletableFuture<AssumeRoleResponse> = client.assumeRole(request)
                val resp = response.get()

                val expiration = DateTime(DateUtil.parseUTC(resp.body.credentials.expiration)).setTimeZone(TimeZone.getTimeZone("GMT+8"))
                val duration = DateUtil.between(Date(), expiration, DateUnit.SECOND)
                val ttl = duration - properties.tokenExpireBufferSeconds

                redisUtil.apply {
                    setEx(properties.redisTokenKey, resp.body.credentials.securityToken, ttl, TimeUnit.SECONDS)
                    setEx(properties.redisAccessKeyId, resp.body.credentials.accessKeyId, ttl, TimeUnit.SECONDS)
                    setEx(properties.redisAccessKeySecretKey, resp.body.credentials.accessKeySecret, ttl, TimeUnit.SECONDS)
                }

                client.close()
                return
            } catch (e: Exception) {
                logger.error("Token refresh failed", e)
                if (++retryCount >= maxRetries) {
                    throw BusinessException(CommonErrorCode.INTERNAL_ERROR, "Token刷新失败: ${e.message}")
                }
                Thread.sleep(1000L * retryCount)
            }
        }
    }

    private fun checkExistingToken(): TokenStatus {
        val token = redisUtil.get(properties.redisTokenKey)?.toString() ?: ""
        val accessKey = redisUtil.get(properties.redisAccessKeyId)?.toString() ?: ""
        val accessSecret = redisUtil.get(properties.redisAccessKeySecretKey)?.toString() ?: ""
        val ttl = redisUtil.getTTL(properties.redisTokenKey)
        return TokenStatus(token, accessKey, accessSecret, ttl)
    }

    private fun tryAcquireLock(): Boolean {
        return redisUtil.acquireLock(properties.lockKey, properties.lockExpireMillis)
    }

    private fun releaseLock() {
        redisUtil.releaseLock(properties.lockKey)
    }

    private fun waitAndRetry(): Map<String, String> {
        Thread.sleep(100)
        return getToken()
    }
}
