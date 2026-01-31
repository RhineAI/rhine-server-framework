package com.rhineai.framework.entity.vo.alioss

data class TokenStatus(
    val token: String?,
    val accessKey: String?,
    val accessSecret: String?,
    val ttl: Long
) {
    val missing: Boolean get() = token == null || accessKey == null || accessSecret == null
    val expiring: Boolean get() = ttl < 300  // 5分钟
    fun toMap() = mapOf(
        "token" to token!!,
        "accessKey" to accessKey!!,
        "accessSecret" to accessSecret!!
    )
}