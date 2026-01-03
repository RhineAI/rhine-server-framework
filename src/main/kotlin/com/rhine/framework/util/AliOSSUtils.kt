package com.rhine.framework.util

import cn.hutool.core.date.DateTime
import cn.hutool.core.date.DateUnit
import cn.hutool.core.date.DateUtil
import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.auth.DefaultCredentialProvider
import com.aliyun.sdk.service.sts20150401.AsyncClient
import com.aliyun.sdk.service.sts20150401.models.AssumeRoleRequest
import com.aliyun.sdk.service.sts20150401.models.AssumeRoleResponse
import com.rhine.framework.constant.CommonErrorCode
import com.rhine.framework.entity.vo.alioss.TokenStatus
import com.rhine.framework.exception.BusinessException
import darabonba.core.client.ClientOverrideConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Component
@Configuration
class AliOSSUtils(private var redisUtil: RedisUtil) {

    @Value("\${oss.access-key-id}")
    lateinit var accessKeyId: String

    @Value("\${oss.access-key-secret}")
    lateinit var accessKeySecret: String

    @Value("\${oss.role.arn}")
    lateinit var arn: String

    @Value("\${oss.role.session-name}")
    lateinit var sessionName: String

    @Value("\${oss.region}")
    lateinit var region: String

    @Value("\${oss.endpoint}")
    lateinit var endpoint: String

    @Value("\${oss.sts-endpoint}")
    lateinit var stsEndpoint: String

    @Value("\${oss.redis-token-key}")
    lateinit var redisTokenKey: String

    @Value("\${oss.redis-access-key-secret-key}")
    lateinit var redisAccessSecret: String

    @Value("\${oss.redis-access-key-id}")
    lateinit var redisAccessId: String

    @Value("\${oss.bucket-name}")
    lateinit var bucketName: String

    // 分布式锁相关配置
    private val lockKey = "lock:oss:token"
    private val lockExpire = 5000L  // 锁有效期5秒

    private val logger = LoggerFactory.getLogger(AliOSSUtils::class.java)

    fun getAuthToken() {
        val provider: StaticCredentialProvider = StaticCredentialProvider.create(
            Credential.builder() // Please ensure that the environment variables ALIBABA_CLOUD_ACCESS_KEY_ID and ALIBABA_CLOUD_ACCESS_KEY_SECRET are set.
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret) //.securityToken(System.getenv("ALIBABA_CLOUD_SECURITY_TOKEN")) // use STS token
                .build()
        )
        // Configure the Client
        val client: AsyncClient = AsyncClient.builder()
            .region(region) // Region ID
            .credentialsProvider(provider) //.serviceConfiguration(Configuration.create()) // Service-level configuration
            .overrideConfiguration(
                ClientOverrideConfiguration.create() // Endpoint 请参考 https://api.aliyun.com/product/Sts
                    .setEndpointOverride(stsEndpoint)
                    .setConnectTimeout(Duration.ofSeconds(43200))
            )
            .build()
        val assumeRoleRequest: AssumeRoleRequest =
            AssumeRoleRequest.builder()
                .roleArn(arn)
                .roleSessionName(sessionName)
                .build()
        val response: CompletableFuture<AssumeRoleResponse> = client.assumeRole(assumeRoleRequest)
        val resp: AssumeRoleResponse = response.get()
        val expirationTime: Long = DateUtil.between(
            DateUtil.parseDate(DateUtil.now()),
            DateUtil.parseDate(resp.body.credentials.expiration), DateUnit.MS
        )
        redisUtil.setEx(redisTokenKey, resp.body.credentials.securityToken, expirationTime, TimeUnit.MICROSECONDS)
        redisUtil.setEx(redisAccessSecret, resp.body.credentials.accessKeySecret, expirationTime, TimeUnit.MICROSECONDS)
        redisUtil.setEx(redisAccessId, resp.body.credentials.accessKeyId, expirationTime, TimeUnit.MICROSECONDS)
        client.close()
    }

    /**
     * 获取认证Token（带自动刷新机制）
     */
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

    /**
     * 刷新Token核心逻辑
     */
    private fun refreshToken(maxRetries: Int = 3) {
        var retryCount = 0
        while (retryCount < maxRetries) {
            try {
                val provider = StaticCredentialProvider.create(
                    Credential.builder()
                        .accessKeyId(accessKeyId)
                        .accessKeySecret(accessKeySecret)
                        .build()
                )

                val client = AsyncClient.builder()
                    .region(region)
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                            .setEndpointOverride(stsEndpoint)
                    )
                    .build()

                val request = AssumeRoleRequest.builder()
                    .roleArn(arn)
                    .roleSessionName(sessionName)
                    .build()

                val response: CompletableFuture<AssumeRoleResponse> = client.assumeRole(request)
                val resp = response.get()

                // 计算正确的时间差（秒）
                val expiration = DateTime(DateUtil.parseUTC(resp.body.credentials.expiration)).setTimeZone(TimeZone.getTimeZone("GMT+8"))
                val duration = DateUtil.between(Date(), expiration, DateUnit.SECOND)

                // 存储到Redis（提前10秒过期）
                redisUtil.apply {
                    setEx(redisTokenKey, resp.body.credentials.securityToken, duration - 10, TimeUnit.SECONDS)
                    setEx(redisAccessId, resp.body.credentials.accessKeyId, duration - 10, TimeUnit.SECONDS)
                    setEx(redisAccessSecret, resp.body.credentials.accessKeySecret, duration - 10, TimeUnit.SECONDS)
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

    fun getFilePath(objectName: String): String {
        val token = getToken()
        val credentialsProvider =
            DefaultCredentialProvider(
                token["accessKey"].toString(),
                token["accessSecret"].toString(),
                token["token"].toString()
            )
        val ossClient = OSSClientBuilder().build(endpoint, credentialsProvider)
        val expiration: Date = Date(Date().time + 3600 * 1000L)
        val url: URL = ossClient.generatePresignedUrl(bucketName, objectName, expiration)
        return url.path
    }

    /**
     * 直接上传文件流到OSS（适用于非Multipart请求）
     * @param inputStream 文件输入流
     * @param objectName OSS文件路径（如 "images/2023/test.jpg"）
     * @return 文件在OSS的完整URL
     */
    fun uploadStream(inputStream: InputStream, objectName: String): String {
        val token = getToken()
        val credentialsProvider = DefaultCredentialProvider(
            token["accessKey"].toString(),
            token["accessSecret"].toString(),
            token["token"].toString()
        )

        val ossClient = OSSClientBuilder().build(endpoint, credentialsProvider)
        return try {
            ossClient.putObject(bucketName, objectName, inputStream)
            generateFileUrl(objectName) // 生成文件访问URL
        } finally {
            ossClient.shutdown()
            inputStream.close()
        }
    }

    /**
     * 生成文件访问URL（私有方法复用）
     */
    private fun generateFileUrl(objectName: String): String {
        val ossClient = OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret)
        val expiration = Date(Date().time + 3600 * 1000L) // 1小时有效期
        return ossClient.generatePresignedUrl(bucketName, objectName, expiration).toString()
    }

    fun uploadMultipartStream(fileContent: InputStream, preLabel: String, fileId: String, ext: String): String {
        val logger = LoggerFactory.getLogger(this::class.java)
        return fileContent.use {
            try {
                val filePath = FileUtils().getFilePath(fileId, ext)
                uploadStream(it, "$preLabel$filePath")
            } catch (e: Exception) {
                logger.error("Failed to upload file with fileId: $fileId, ext: $ext", e)
                throw BusinessException(CommonErrorCode.INTERNAL_ERROR, "File upload failed: ${e.message}")
            }
        }
    }

    private fun checkExistingToken(): TokenStatus {
        val token = redisUtil.get(redisTokenKey)?.toString() ?: ""
        val accessKey = redisUtil.get(redisAccessId)?.toString() ?: ""
        val accessSecret = redisUtil.get(redisAccessSecret)?.toString() ?: ""
        val ttl = redisUtil.getTTL(redisTokenKey)
        return TokenStatus(token, accessKey, accessSecret, ttl)
    }

    private fun tryAcquireLock(): Boolean {
        return redisUtil.acquireLock(lockKey, lockExpire)
    }

    private fun releaseLock() {
        redisUtil.releaseLock(lockKey)
    }

    private fun waitAndRetry(): Map<String, String> {
        Thread.sleep(100)
        return getToken()
    }
}