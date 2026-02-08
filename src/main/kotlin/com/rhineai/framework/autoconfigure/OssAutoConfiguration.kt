package com.rhineai.framework.autoconfigure

import com.rhineai.framework.oss.OssProperties
import com.rhineai.framework.oss.OssService
import com.rhineai.framework.oss.aliyun.AliyunOssProperties
import com.rhineai.framework.oss.aliyun.AliyunOssService
import com.rhineai.framework.oss.aliyun.AliyunOssTokenManager
import com.rhineai.framework.util.AliOSSUtils
import com.rhineai.framework.util.RedisUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [OssProperties::class, AliyunOssProperties::class])
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = ["enabled"], havingValue = "true")
@ConditionalOnProperty(prefix = OssProperties.PREFIX, name = ["provider"], havingValue = "aliyun")
class OssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AliyunOssProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun aliyunOssTokenManager(redisUtil: RedisUtil, properties: AliyunOssProperties): AliyunOssTokenManager {
        return AliyunOssTokenManager(redisUtil, properties)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AliyunOssProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun aliyunOssService(properties: AliyunOssProperties, tokenManager: AliyunOssTokenManager): AliyunOssService {
        return AliyunOssService(properties, tokenManager)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AliyunOssProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun ossService(aliyunOssService: AliyunOssService): OssService {
        return aliyunOssService
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AliyunOssProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun aliOSSUtils(tokenManager: AliyunOssTokenManager, aliyunOssService: AliyunOssService): AliOSSUtils {
        return AliOSSUtils(tokenManager, aliyunOssService)
    }
}
