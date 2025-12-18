package com.rhine.framework.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

@Configuration
@EnableConfigurationProperties(FormatProperties::class)
class JacksonFormatConfig {

    @Bean
    @ConditionalOnProperty(prefix = FormatProperties.PREFIX, name = ["long-to-string"], havingValue = "true")
    fun longToStringCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializerByType(Long::class.java, ToStringSerializer.instance)
            builder.serializerByType(Long::class.javaObjectType, ToStringSerializer.instance)
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = FormatProperties.PREFIX, name = ["date-to-timestamp-string"], havingValue = "true")
    fun dateToTimestampStringCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializerByType(Date::class.java, DateToTimestampStringSerializer())
            builder.serializerByType(Instant::class.java, InstantToTimestampStringSerializer())
            builder.serializerByType(LocalDateTime::class.java, LocalDateTimeToTimestampStringSerializer())
            builder.serializerByType(ZonedDateTime::class.java, ZonedDateTimeToTimestampStringSerializer())
        }
    }

    class DateToTimestampStringSerializer : JsonSerializer<Date>() {
        override fun serialize(value: Date?, gen: JsonGenerator, serializers: SerializerProvider) {
            if (value == null) {
                gen.writeNull()
            } else {
                gen.writeString(value.time.toString())
            }
        }
    }

    class InstantToTimestampStringSerializer : JsonSerializer<Instant>() {
        override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
            if (value == null) {
                gen.writeNull()
            } else {
                gen.writeString(value.toEpochMilli().toString())
            }
        }
    }

    class LocalDateTimeToTimestampStringSerializer : JsonSerializer<LocalDateTime>() {
        override fun serialize(value: LocalDateTime?, gen: JsonGenerator, serializers: SerializerProvider) {
            if (value == null) {
                gen.writeNull()
            } else {
                gen.writeString(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli().toString())
            }
        }
    }

    class ZonedDateTimeToTimestampStringSerializer : JsonSerializer<ZonedDateTime>() {
        override fun serialize(value: ZonedDateTime?, gen: JsonGenerator, serializers: SerializerProvider) {
            if (value == null) {
                gen.writeNull()
            } else {
                gen.writeString(value.toInstant().toEpochMilli().toString())
            }
        }
    }
}
