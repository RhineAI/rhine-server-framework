package com.rhineai.framework.util

data class SmsRequest(
    val mobile: String,                    // 目标手机号
    val content: String,                   // 短信内容
    val signName: String = "一童视界", // 短信签名，默认值
    val templateCode: String = "SMS_491140130" // 模板代码，默认值
) {
    // 将内容格式化为模板参数
    fun toTemplateParam(): String = """{"code":"$content"}"""
}