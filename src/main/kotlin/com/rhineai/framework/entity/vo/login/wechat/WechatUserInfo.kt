package com.rhineai.framework.entity.vo.login.wechat


data class WechatUserInfo(
    var openid: String? = null,          // 微信用户唯一标识
    var nickname: String? = null,        // 用户昵称
    var sex: Int? = null,                // 性别（0未知，1男，2女）
    var language: String? = null,        // 用户语言
    var city: String? = null,            // 所在城市
    var province: String? = null,        // 所在省份
    var country: String? = null,         // 所在国家
    var headimgurl: String? = null,      // 头像URL
    var privilege: List<Any>? = null,          // 用户权限信息（建议替换为具体类型）
    var unionid: String? = null,        // 跨平台用户唯一标识（如微信多账号登录）
    var error: Int? = null,          // 跨平台用户唯一标识（如微信多账号登录）
    var message: String? = null          // 跨平台用户唯一标识（如微信多账号登录）
)