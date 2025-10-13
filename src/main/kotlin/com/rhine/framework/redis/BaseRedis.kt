package com.rhine.framework.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class BaseRedis(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    fun setString(key: String, value: String, timeout: Long? = null, unit: TimeUnit = TimeUnit.SECONDS) {
        redisTemplate.opsForValue().set(key, value)
        if (timeout != null) redisTemplate.expire(key, timeout, unit)
    }

    fun getString(key: String): String? = redisTemplate.opsForValue().get(key) as? String

    fun setObject(key: String, value: Any, timeout: Long? = null, unit: TimeUnit = TimeUnit.SECONDS) {
        redisTemplate.opsForValue().set(key, value)
        if (timeout != null) redisTemplate.expire(key, timeout, unit)
    }

    fun getObject(key: String): Any? = redisTemplate.opsForValue().get(key)

    fun delKey(key: String) { redisTemplate.delete(key) }
}