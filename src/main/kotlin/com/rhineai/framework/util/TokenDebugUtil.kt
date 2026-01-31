package com.rhineai.framework.util

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class TokenDebugUtil(
    private val redisTemplate: StringRedisTemplate
) {
    
    companion object {
        private const val ACCESS_TOKEN_PREFIX = "oauth2:token:access:"
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
        private const val OAUTH2_TOKEN_PREFIX = "oauth2:token:"
    }

    /**
     * 检查access token是否存在于redis中
     */
    fun checkAccessToken(token: String): Map<String, Any?> {
        val key = "$ACCESS_TOKEN_PREFIX$token"
        val exists = redisTemplate.hasKey(key)
        val value = if (exists) redisTemplate.opsForValue().get(key) else null
        val ttl = if (exists) redisTemplate.getExpire(key) else -1
        
        return mapOf(
            "key" to key,
            "exists" to exists,
            "value" to value,
            "ttl" to ttl
        )
    }

    /**
     * 列出所有以oauth2:token:access:开头的key
     */
    fun listAccessTokens(): Set<String> {
        return redisTemplate.keys("$ACCESS_TOKEN_PREFIX*")
    }

    /**
     * 列出所有oauth2相关的token
     */
    fun listAllOAuth2Tokens(): Map<String, Set<String>> {
        return mapOf(
            "access_tokens" to redisTemplate.keys("$ACCESS_TOKEN_PREFIX*"),
            "refresh_tokens" to redisTemplate.keys("$REFRESH_TOKEN_PREFIX*"),
            "oauth2_tokens" to redisTemplate.keys("$OAUTH2_TOKEN_PREFIX*")
        )
    }

    /**
     * 清理所有token（用于测试）
     */
    fun clearAllTokens() {
        listOf("$ACCESS_TOKEN_PREFIX*", "$REFRESH_TOKEN_PREFIX*", "$OAUTH2_TOKEN_PREFIX*")
            .forEach { pattern ->
                redisTemplate.keys(pattern).forEach { key ->
                    redisTemplate.delete(key)
                }
            }
    }
} 