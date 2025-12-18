package com.rhine.framework.annotation.permission

import com.rhine.framework.exception.NoPermissionException
import com.rhine.framework.redis.BaseRedis
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
class EnableCheckPermissionConfig(
    private val stringRedisTemplate: StringRedisTemplate,
    private val baseRedis: BaseRedis
) {

    @Bean
    fun checkPermissionAspect(): CheckPermissionAspect = object : CheckPermissionAspect() {
        override fun getUserHasPermission(userId: String?): List<String> {
            if (userId.isNullOrBlank()) throw NoPermissionException()

            // Feature-level permissions stored as Redis Set: fp:{userId}:feature
            val featureKey = "fp:$userId:feature"
            val featureIds = runCatching { stringRedisTemplate.opsForSet().members(featureKey) }
                .getOrNull()
                ?.map { it.toString() }
                ?: emptyList()

            // Menu-level permissions stored as Redis Set: fp:{userId}:menu (optional)
            val menuKey = "fp:$userId:menu"
            val menuIds = runCatching { stringRedisTemplate.opsForSet().members(menuKey) }
                .getOrNull()
                ?.map { it.toString() }
                ?: emptyList()

            // Legacy map storage: user:promission:{userId} -> { "user:promission:": [codes...] }
            val legacyKey = "user:promission:$userId"
            val legacyRaw = baseRedis.getObject(legacyKey)
            val legacyList: List<String> = if (legacyRaw is Map<*, *>) {
                val v = legacyRaw["user:promission:"]
                when (v) {
                    is Collection<*> -> v.mapNotNull { it?.toString() }
                    is Array<*> -> v.mapNotNull { it?.toString() }
                    is String -> listOf(v)
                    else -> emptyList()
                }
            } else emptyList()

            // Add roles from JWT token if available in SecurityContext
            val roles: List<String> = runCatching {
                val context = SecurityContextHolder.getContext()
                val auth = context.authentication
                if (auth != null && auth.isAuthenticated) {
                    auth.authorities.map { it.authority }
                } else {
                    emptyList()
                }
            }.getOrElse { emptyList() }

            return (featureIds + menuIds + legacyList + roles).distinct()
        }
    }
}
