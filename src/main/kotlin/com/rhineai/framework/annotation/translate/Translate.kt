package com.rhineai.framework.annotation.translate

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@JacksonAnnotationsInside
@JsonSerialize(using = TranslatorSerializer::class)
annotation class Translate(
    val translator: KClass<out Translator<*>>
)