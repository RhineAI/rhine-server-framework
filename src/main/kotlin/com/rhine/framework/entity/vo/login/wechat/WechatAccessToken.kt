package com.rhine.framework.entity.vo.login.wechat

data class WechatAccessToken(
    var access_token:String? = null,
    var expires_in:Long? = null,
    var refresh_token:String? = null,
    var openid:String? = null,
    var scope:String? = null,
    var unionid:String? = null,
    var error: Int? = null,
    var message:String? = null,
    var fetchDatetime: Long? = null,
)