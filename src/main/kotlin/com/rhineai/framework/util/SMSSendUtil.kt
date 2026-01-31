package com.rhineai.framework.util

import com.aliyun.auth.credentials.Credential
import com.aliyun.auth.credentials.provider.StaticCredentialProvider
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse
import com.google.gson.Gson
import darabonba.core.client.ClientOverrideConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
@Configuration
class SMSSendUtil {

    private val logger = LoggerFactory.getLogger(SMSSendUtil::class.java)

    @Value("\${aliyun.sms.access-key-id}")
    lateinit var accessKeyId: String

    @Value("\${aliyun.sms.access-key-secret}")
    lateinit var accessKeySecret: String

    private val client: AsyncClient by lazy {
        val provider = StaticCredentialProvider.create(
            Credential.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build()
        )

        AsyncClient.builder()
            .region("cn-qingdao") // 根据需要调整区域
            .credentialsProvider(provider)
            .overrideConfiguration(
                ClientOverrideConfiguration.create()
                    .setEndpointOverride("dysmsapi.aliyuncs.com")
            )
            .build()
    }

    @Throws(Exception::class)
    fun sendSms(request: SmsRequest): SendSmsResponse {
        val smsRequest = SendSmsRequest.builder()
            .signName(request.signName)
            .templateCode(request.templateCode)
            .phoneNumbers(request.mobile)
            .templateParam(request.toTemplateParam())
            .build()

        val response: CompletableFuture<SendSmsResponse> = client.sendSms(smsRequest)
        val resp = response.get() // 同步获取结果
        logger.info("SMS Send Response: {}", Gson().toJson(resp))
        return resp
    }

    @Throws(Exception::class)
    fun sendSms(mobile: String, content: String): SendSmsResponse {
        return sendSms(SmsRequest(mobile, content))
    }

    // 关闭客户端（可选，在 Spring 中通常由容器管理）
    fun close() {
        client.close()
    }
}