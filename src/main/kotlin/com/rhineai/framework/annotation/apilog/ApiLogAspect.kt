package com.rhineai.framework.annotation.apilog

import com.google.gson.Gson
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
class ApiLogAspect(
    private val properties: ApiLogProperties
) {
    private val log = LoggerFactory.getLogger(ApiLogAspect::class.java)
    private val startTimeTL = ThreadLocal<Long>()

    @Pointcut("@annotation(com.rhineai.framework.annotation.apilog.ApiLog)")
    fun apiLogPointcut() {}

    @Before("apiLogPointcut()")
    fun before(joinPoint: JoinPoint) {
        if (!properties.enabled) return
        startTimeTL.set(System.currentTimeMillis())
        val request = currentRequest() ?: return
        log.info("========================================== Start ==========================================")
        log.info("接口地址           : {}", request.requestURL.toString())
        log.info("HTTP Method       : {}", request.method)
        log.info("Class Method      : {}.{}", joinPoint.signature.declaringTypeName, joinPoint.signature.name)
        log.info("IP                : {}", request.remoteAddr)
    }

    @Around("apiLogPointcut()")
    fun around(pjp: ProceedingJoinPoint): Any? {
        val result = pjp.proceed()
        if (!properties.enabled) return result
        log.info("响应参数          : {}", safeJson(result))
        return result
    }

    @After("apiLogPointcut()")
    fun after() {
        if (!properties.enabled) return
        val duration = System.currentTimeMillis() - (startTimeTL.get() ?: System.currentTimeMillis())
        log.info("耗时              : {} ms", duration)
        log.info("=========================================== End ===========================================\n")
        startTimeTL.remove()
    }

    private fun currentRequest(): HttpServletRequest? {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
        return attrs?.request
    }

    private fun safeJson(obj: Any?): String {
        return try { Gson().toJson(obj) } catch (e: Exception) {
            e.printStackTrace()
            obj?.toString() ?: "null"
        }
    }
}