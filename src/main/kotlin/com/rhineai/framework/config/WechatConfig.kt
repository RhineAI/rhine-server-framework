package com.rhineai.framework.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@Configuration
class WechatConfig {
    @Value("\${wechat.app-key}")
    lateinit var accessKeyId: String

    @Value("\${wechat.app-secret}")
    lateinit var accessKeySecret: String
}