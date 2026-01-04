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
            val auth = SecurityContextHolder.getContext()?.authentication
            if (auth != null && auth.isAuthenticated && auth.name == "admin") {
                return listOf("*")
            }

            val roleNames: List<String> = runCatching {
                if (auth != null && auth.isAuthenticated) {
                    auth.authorities.mapNotNull { it?.authority }.filter { it.isNotBlank() }
                } else {
                    emptyList()
                }
            }.getOrElse { emptyList() }

            val rolePermCodes: List<String> = if (roleNames.isNotEmpty()) {
                roleNames.flatMap { roleName ->
                    val roleKey = "perm:role:$roleName"
                    runCatching { stringRedisTemplate.opsForSet().members(roleKey) }
                        .getOrNull()
                        ?.map { it.toString() }
                        ?: emptyList()
                }
            } else {
                emptyList()
            }

            val userPermCodes: List<String> = if (!userId.isNullOrBlank()) {
                val permKey = "perm:$userId"
                runCatching { stringRedisTemplate.opsForSet().members(permKey) }
                    .getOrNull()
                    ?.map { it.toString() }
                    ?: emptyList()
            } else {
                emptyList()
            }

            // Legacy map storage: user:promission:{userId} -> { "user:promission:": [codes...] }
            val legacyList: List<String> = if (!userId.isNullOrBlank()) {
                val legacyKey = "user:promission:$userId"
                val legacyRaw = baseRedis.getObject(legacyKey)
                if (legacyRaw is Map<*, *>) {
                    val v = legacyRaw["user:promission:"]
                    when (v) {
                        is Collection<*> -> v.mapNotNull { it?.toString() }
                        is Array<*> -> v.mapNotNull { it?.toString() }
                        is String -> listOf(v)
                        else -> emptyList()
                    }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }

            val merged = (rolePermCodes + userPermCodes + legacyList).distinct()
            if (merged.isEmpty()) throw NoPermissionException()
            return merged
        }
    }
}
