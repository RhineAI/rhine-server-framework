package com.rhine.framework.annotation.permission

enum class Logical {
    AND, OR
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Permission(
    vararg val value: String,
    val logical: Logical = Logical.OR
)
