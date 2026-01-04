package com.rhine.framework.annotation.permission

import com.rhine.framework.exception.NoPermissionException
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

import org.slf4j.LoggerFactory

@Aspect
abstract class CheckPermissionAspect {
    
    private val logger = LoggerFactory.getLogger(CheckPermissionAspect::class.java)

    @Pointcut("@annotation(com.rhine.framework.annotation.permission.Permission)")
    fun checkPermissionPointcut() {}

    @Before("checkPermissionPointcut()")
    fun checkPermissionBefore(joinPoint: JoinPoint) {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw NoPermissionException()
        val request = attrs.request
        val userId = request.getHeader("currentUserId")
        val permissions = getUserHasPermission(userId)
        
        val permissionAnn = getAspectPermission(joinPoint) ?: throw NoPermissionException()
        val required = permissionAnn.value
        val logical = permissionAnn.logical

        if (required.isEmpty()) return

        // Filter out empty strings from required permissions just in case
        val validRequired = required.filter { it.isNotBlank() }
        if (validRequired.isEmpty()) return

        if (permissions.isNullOrEmpty()) {
            logger.warn("Permission denied for user [$userId]. User has no permissions. Required: ${validRequired.joinToString()}")
            throw NoPermissionException()
        }

        if (permissions.contains("*")) {
            return
        }

        val hasPermission = when (logical) {
            Logical.AND -> validRequired.all { permissions.contains(it) }
            Logical.OR -> validRequired.any { permissions.contains(it) }
        }

        if (!hasPermission) {
            logger.warn("Permission denied for user [$userId]. Required: ${validRequired.joinToString()}, Logical: $logical, User Permissions: $permissions")
            throw NoPermissionException()
        }
    }

    abstract fun getUserHasPermission(userId: String?): List<String>

    @Throws(Exception::class)
    fun getAspectPermission(joinPoint: JoinPoint): Permission? {
        val targetClass = joinPoint.target.javaClass
        val methodName = joinPoint.signature.name
        val argCount = joinPoint.args.size
        val methods = targetClass.methods
        for (m in methods) {
            if (m.name == methodName && m.parameterTypes.size == argCount) {
                val ann = m.getAnnotation(Permission::class.java)
                if (ann != null) return ann
            }
        }
        return null
    }
}
