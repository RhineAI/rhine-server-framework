package com.rhine.framework.annotation.translate.dictionary

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.rhine.framework.annotation.translate.Translate

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@JacksonAnnotationsInside
@Translate(translator = DictTranslator::class)
annotation class DictTranslate(
    val dictType: String = "",
    val publicKey: String = ""
)