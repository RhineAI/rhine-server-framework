package com.rhineai.framework.entity.vo

import java.time.Instant

data class ErrorResponse(
    val code: String,
    val message: String,
    val path: String? = null,
    val timestamp: Instant = Instant.now()
)