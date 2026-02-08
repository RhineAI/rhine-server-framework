package com.rhineai.framework.oss

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = OssProperties.PREFIX)
class OssProperties {
    companion object { const val PREFIX = "framework.oss" }
    var enabled: Boolean = false
    var provider: String = "aliyun"
}
