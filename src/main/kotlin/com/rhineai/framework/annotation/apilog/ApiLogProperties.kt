package com.rhineai.framework.annotation.apilog

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = ApiLogProperties.PREFIX)
class ApiLogProperties {
    companion object { const val PREFIX = "framework.api-log" }
    var model: String? = null
    var enabled: Boolean = true
}