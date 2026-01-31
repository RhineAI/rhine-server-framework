package com.rhineai.framework.test

import com.rhineai.framework.annotation.apilog.ApiLog
import com.rhineai.framework.constant.CommonErrorCode
import com.rhineai.framework.exception.BusinessException
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class DummyController {

    @GetMapping("/api/ok")
    @ApiLog(description = "ok endpoint")
    fun ok(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf("status" to "OK"))
    }

    @GetMapping("/api/biz")
    fun biz(): ResponseEntity<Void> {
        throw BusinessException(CommonErrorCode.USER_NOT_FOUND)
    }

    @GetMapping("/api/invalid")
    fun invalid(@RequestParam("p") p: String): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf("p" to p))
    }

    @GetMapping("/api/oops")
    fun oops(): ResponseEntity<Void> {
        throw RuntimeException("boom")
    }
}