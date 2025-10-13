package com.rhine.framework.annotation.permission

import com.rhine.framework.exception.NoPermissionException
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
abstract class CheckPermissionAspect {

    @Pointcut("@annotation(com.rhine.framework.annotation.permission.CheckPermission)")
    fun checkPermissionPointcut() {}

    @Before("checkPermissionPointcut()")
    fun checkPermissionBefore(joinPoint: JoinPoint) {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw NoPermissionException()
        val request = attrs.request
        val userId = request.getHeader("currentUserId")
        val permissions = getUserHasPermission(userId)
        if (permissions.isNullOrEmpty()) throw NoPermissionException()
        val required = getAspectPermission(joinPoint)
        if (required.isBlank() || !permissions.contains(required)) throw NoPermissionException()
    }

    abstract fun getUserHasPermission(userId: String?): List<String>

    @Throws(Exception::class)
    fun getAspectPermission(joinPoint: JoinPoint): String {
        val targetClass = joinPoint.target.javaClass
        val methodName = joinPoint.signature.name
        val argCount = joinPoint.args.size
        val methods = targetClass.methods
        for (m in methods) {
            if (m.name == methodName && m.parameterTypes.size == argCount) {
                val ann = m.getAnnotation(CheckPermission::class.java)
                if (ann != null) return ann.permission
            }
        }
        return ""
    }
}