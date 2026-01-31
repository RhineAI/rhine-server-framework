package com.rhineai.framework.autoconfigure

import com.rhineai.framework.annotation.apilog.ApiLogAspect
import com.rhineai.framework.annotation.apilog.ApiLogProperties
import com.rhineai.framework.config.JacksonFormatConfig
import com.rhineai.framework.config.RedisConfig
import com.rhineai.framework.config.OpenApiConfig
import com.rhineai.framework.redis.BaseRedis
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import com.rhineai.framework.annotation.datapermission.DataPermissionAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import com.rhineai.framework.annotation.datapermission.DataPermissionProperties
import com.rhineai.framework.annotation.datapermission.AclIdsProvider
import org.springframework.data.redis.core.RedisTemplate
import com.rhineai.framework.spring.SpringContextHolder

@Configuration
@EnableConfigurationProperties(value = [ApiLogProperties::class, DataPermissionProperties::class])
@Import(RedisConfig::class, OpenApiConfig::class, JacksonFormatConfig::class)
class RhineFrameworkAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = ApiLogProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    fun apiLogAspect(properties: ApiLogProperties): ApiLogAspect = ApiLogAspect(properties)

    @Bean
    @ConditionalOnMissingBean
    fun baseRedis(redisTemplate: RedisTemplate<String, Any>): BaseRedis = BaseRedis(redisTemplate)

    @Bean
    @ConditionalOnMissingBean
    fun restExceptionHandler(): com.rhineai.framework.exception.RestExceptionHandler = com.rhineai.framework.exception.RestExceptionHandler()


    @Bean
    @ConditionalOnMissingBean
    fun springContextHolder(): SpringContextHolder = SpringContextHolder()

    // Data permission aspect: works with JPA to enable a Hibernate filter per request
    @Bean
    @ConditionalOnClass(EntityManager::class)
    @ConditionalOnBean(EntityManagerFactory::class)
    @ConditionalOnMissingBean(DataPermissionAspect::class)
    @ConditionalOnProperty(prefix = DataPermissionProperties.PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun dataPermissionAspect(entityManager: EntityManager, properties: DataPermissionProperties, providers: List<AclIdsProvider>): DataPermissionAspect = DataPermissionAspect(entityManager, properties, providers)
}