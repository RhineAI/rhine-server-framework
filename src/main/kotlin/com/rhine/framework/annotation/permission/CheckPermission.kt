package com.rhine.framework.annotation.permission

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CheckPermission(
    val permission: String = ""
)