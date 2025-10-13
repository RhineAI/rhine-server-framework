package com.rhine.framework.constant

enum class CommonErrorCode(val code: String, val message: String) {
    // 用户相关错误
    USER_NOT_FOUND("E00010001", "用户不存在"),
    INVALID_USERNAME("E00011002", "JWT 中未包含有效的用户名"),
    USER_ALREADY_EXISTS("E00011003", "用户已存在"),
    FREQUENT_OPERATION("E00010004","短信发送上限，请明日再试"),
    
    // 认证和登录相关错误
    AUTHENTICATION_FAILED("E00012001", "认证失败"),
    TOKEN_EXPIRED("E00012002", "Token 已过期"),
    USER_CONNECTION_EXISTS("E00012003", "用户已绑定微信"),
    INVALID_CREDENTIALS("E00012004", "用户名或密码错误"),
    INVALID_MOBILE_CREDENTIALS("E00012005", "手机号或密码错误"),
    INVALID_VERIFICATION_CODE("E00012006", "验证码不正确"),
    EXPIRED_VERIFICATION_CODE("E00012007", "验证码已过期"),
    MOBILE_NOT_FOUND("E00012008", "手机号未注册"),
    INVALID_REFRESH_TOKEN("E00012009", "无效的Refresh Token"),

    // 权限相关错误
    NO_PERMISSION("E00014001", "权限不足"),
    
    // 参数相关错误
    INVALID_PARAMETER("E00013001", "参数错误"),

    // 系统错误
    INTERNAL_ERROR("E00000001", "内部服务器错误");
}