package com.rhine.framework.exception

import com.rhine.framework.constant.StatusCode


class BusinessException(
    private val errorCode: StatusCode, // 使用枚举
    override val message: String = errorCode.message // 默认使用枚举中的消息
) : RuntimeException(message) {
    val code: String
        get() = errorCode.code // 提供 code 属性
}