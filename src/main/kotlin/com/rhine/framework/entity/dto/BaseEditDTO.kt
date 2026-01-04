package com.rhine.framework.entity.dto

import java.io.Serializable

interface BaseEditDTO<T> : BaseDTO {
    val id: T
}
