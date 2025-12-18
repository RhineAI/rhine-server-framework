package com.rhine.framework.autoconfigure

import com.rhine.framework.annotation.apilog.ApiLogAspect
import com.rhine.framework.annotation.apilog.ApiLogProperties
import com.rhine.framework.config.JacksonFormatConfig
import com.rhine.framework.config.RedisConfig
import com.rhine.framework.config.OpenApiConfig
import com.rhine.framework.redis.BaseRedis
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import com.rhine.framework.annotation.datapermission.DataPermissionAspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import com.rhine.framework.annotation.datapermission.DataPermissionProperties
import com.rhine.framework.annotation.datapermission.AclIdsProvider
import org.springframework.data.redis.core.RedisTemplate
import com.rhine.framework.spring.SpringContextHolder

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

    // @Bean
    // @ConditionalOnMissingBean
    // fun restExceptionHandler(): RestExceptionHandler = RestExceptionHandler()

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