package com.rhine.framework.annotation.permission

import org.springframework.context.annotation.Import

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(EnableCheckPermissionConfig::class)
annotation class EnableCheckPermission
