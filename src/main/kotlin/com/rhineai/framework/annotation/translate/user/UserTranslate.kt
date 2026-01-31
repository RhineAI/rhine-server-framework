package com.rhineai.framework.annotation.translate.user

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside
import com.rhineai.framework.annotation.translate.Translate

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
@JacksonAnnotationsInside
@Translate(translator = UserTranslator::class)
annotation class UserTranslate
