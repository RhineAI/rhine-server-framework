package com.rhine.framework.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)

        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        @Suppress("DEPRECATION")
        objectMapper.activateDefaultTyping(objectMapper.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL)

        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(Any::class.java).apply {
            setObjectMapper(objectMapper)
        }

        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = jackson2JsonRedisSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer
        template.afterPropertiesSet()
        return template
    }
}