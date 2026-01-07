package com.rhine.framework.util

import cn.hutool.core.util.StrUtil
import cn.hutool.http.HttpUtil
import cn.hutool.json.JSONUtil
import com.rhine.framework.config.WechatConfig
import com.rhine.framework.entity.vo.login.wechat.WechatAccessToken
import com.rhine.framework.entity.vo.login.wechat.WechatUserInfo
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class WechatAuthUtil(
    private val redisUtil: RedisUtil,
    private val wechatConfig: WechatConfig
) {
    // Redis key模板
    private companion object {
        const val TOKEN_KEY_TEMPLATE = "wechat:token:{}"       // appId + code
        const val USERINFO_KEY_TEMPLATE = "wechat:user:{}"     // appId + openId
        const val REFRESH_LOCK_KEY = "wechat:refresh:lock:{}"  // appId + openId
        const val TOKEN_EXPIRE_SECONDS = 7000L    // 微信token有效期7200秒，提前过期
    }

    /**
     * 获取AccessToken（带自动刷新） // todo 自动刷新失效
     */
    fun getAccessToken(code: String): WechatAccessToken {
        val appId = wechatConfig.accessKeyId
        val appSecret = wechatConfig.accessKeySecret
        val cacheKey = StrUtil.format(TOKEN_KEY_TEMPLATE, appId)

        var wechatAccessToken: WechatAccessToken? = null

        try {
            wechatAccessToken = redisUtil.getBean(cacheKey, WechatAccessToken::class.java) as WechatAccessToken
            if (wechatAccessToken.access_token == null){
                wechatAccessToken = requestAccessToken(code, appId, appSecret)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val lockKey = StrUtil.format(REFRESH_LOCK_KEY, appId)
            return try {
                if (redisUtil.acquireLock(lockKey, 5)) {
                    val freshToken = requestAccessToken(code, appId, appSecret)
                    storeToken(cacheKey, freshToken)
                    freshToken
                } else {
                    Thread.sleep(100)
                    getAccessToken(code)
                }
            } finally {
                redisUtil.releaseLock(lockKey)
            }
        }
        return wechatAccessToken
    }

        /**
         * 获取用户信息（带缓存）
         */
        fun getUserInfo(accessToken: String, openId: String): WechatUserInfo {
            val cacheKey = StrUtil.format(USERINFO_KEY_TEMPLATE, wechatConfig.accessKeyId)
            val cached: WechatUserInfo? = try {
                redisUtil.getBean(cacheKey, WechatUserInfo::class.java) as WechatUserInfo
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val userInfo = cached ?: requestUserInfo(accessToken, openId).also {
                redisUtil.setEx(cacheKey, JSONUtil.toJsonPrettyStr(it), 24, TimeUnit.HOURS)
            }
            return userInfo
        }

        /**
         * 主动刷新Token
         */
        fun refreshToken(code: String): WechatAccessToken {
            val appId = wechatConfig.accessKeyId
            val cacheKey = StrUtil.format(TOKEN_KEY_TEMPLATE, appId)
            val newToken = requestAccessToken(code, appId, wechatConfig.accessKeySecret)
            storeToken(cacheKey, newToken)
            return newToken
        }

        private fun requestAccessToken(code: String, appId: String, appSecret: String): WechatAccessToken {
            val url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                    "appid=$appId&secret=$appSecret&code=$code&grant_type=authorization_code"

            return executeWithRetry(3) {
                val response = HttpUtil.get(url)
                val token = JSONUtil.toBean(response, WechatAccessToken::class.java)
                val fetchTime = System.currentTimeMillis()
                token.fetchDatetime = fetchTime
                validateTokenResponse(token)
                token
            }
        }

        private fun requestUserInfo(accessToken: String, openId: String): WechatUserInfo {
            val url = "https://api.weixin.qq.com/sns/userinfo?" +
                    "access_token=$accessToken&openid=$openId"

            return executeWithRetry(3) {
                val response = HttpUtil.get(url)
                JSONUtil.toBean(response, WechatUserInfo::class.java).apply {
                    if (error != 0) {
//                    throw BusinessException("WECHAT_ERROR", "获取用户信息失败: $errmsg")
                    }
                }
            }
        }

        private fun storeToken(key: String, token: WechatAccessToken) {
            redisUtil.setEx(
                key,
                JSONUtil.toJsonPrettyStr(token),
                TOKEN_EXPIRE_SECONDS,
                TimeUnit.SECONDS
            )
        }

        private fun validateTokenResponse(token: WechatAccessToken) {
            when (token.error) {
//            40029 -> throw BusinessException("WECHAT_ERROR", "无效的授权码")
//            40163 -> throw BusinessException("WECHAT_ERROR", "code已被使用")
//            41008 -> throw BusinessException("WECHAT_ERROR", "缺少授权码参数")
                null -> {} // 正常情况
//            else -> throw BusinessException("WECHAT_ERROR", "微信接口错误: ${token.errmsg}")
            }
        }

        private fun <T> executeWithRetry(maxRetries: Int, block: () -> T): T {
            var retryCount = 0
            while (true) {
                try {
                    return block()
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (retryCount++ >= maxRetries) throw e
                    Thread.sleep(1000L * retryCount)
                }
            }
        }

    private fun isTokenExpiringSoon(token: WechatAccessToken): Boolean {
        val expiresIn = token.expires_in ?: return true
        val fetchTime = token.fetchDatetime ?: return true
        val currentTime = System.currentTimeMillis() / 1000
        val timeLeft = fetchTime + expiresIn - currentTime
        return timeLeft < 300 // Refresh if less than 5 minutes remain
    }
}