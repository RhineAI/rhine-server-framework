package com.rhineai.framework.entity.vo.translator.user

import com.fasterxml.jackson.annotation.JsonIgnore

data class DataUser(
    var id: String? = null,
    var name: String? = null,
    @JsonIgnore
    var status: String? = null,
    var navigator: Boolean? = true,
    @JsonIgnore
    var deleteFlag: Int? = null
)
