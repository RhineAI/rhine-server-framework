package com.rhineai.framework.test

import com.rhineai.framework.annotation.permission.Permission
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

import com.rhineai.framework.annotation.permission.Logical

@RestController
open class ProtectedController {

    @GetMapping("/api/secure/ok")
    @Permission("perm:view")
    open fun secureOk(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(mapOf("ok" to true))

    @GetMapping("/api/secure/deny")
    @Permission("admin:only")
    open fun secureDeny(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(mapOf("ok" to true))

    @GetMapping("/api/secure/or")
    @Permission("perm:view", "admin:only", logical = Logical.OR)
    open fun secureOr(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(mapOf("ok" to true))

    @GetMapping("/api/secure/and")
    @Permission("perm:view", "admin:only", logical = Logical.AND)
    open fun secureAnd(): ResponseEntity<Map<String, Any>> = ResponseEntity.ok(mapOf("ok" to true))
}