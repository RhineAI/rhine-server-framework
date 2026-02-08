package com.rhineai.framework.oss.aliyun

import jakarta.annotation.Resources
import org.springframework.boot.context.properties.ConfigurationProperties

@Resources
@ConfigurationProperties(prefix = AliyunOssProperties.PREFIX)
class AliyunOssProperties {
    companion object { const val PREFIX = "oss" }

    var accessKeyId: String = ""
    var accessKeySecret: String = ""
    var region: String = ""
    var endpoint: String = ""
    var stsEndpoint: String = ""
    var bucketName: String = ""
    var redisTokenKey: String = ""
    var redisAccessKeySecretKey: String = ""
    var redisAccessKeyId: String = ""
    var lockKey: String = "lock:oss:token"
    var lockExpireMillis: Long = 5000L
    var tokenExpireBufferSeconds: Long = 10L
    var enabled: Boolean = true
    var role: Role = Role()

    class Role {
        var arn: String = ""
        var sessionName: String = ""
    }
}
