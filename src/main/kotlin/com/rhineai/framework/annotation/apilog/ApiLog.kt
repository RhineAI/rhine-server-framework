package com.rhineai.framework.annotation.apilog

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ApiLog(
    val description: String = ""
)