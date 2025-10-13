package com.rhine.framework.annotation.translate

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class TranslatorSerializer : StdSerializer<Any>(Any::class.java), ContextualSerializer {
    @Transient
    private var translator: Translator<Any>? = null

    fun getTranslator(): Translator<Any>? = translator
    fun setTranslator(t: Translator<Any>) { translator = t }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty): JsonSerializer<Any> {
        val annotation = property.getAnnotation(Translate::class.java)
        val serializer = TranslatorSerializer()
        val t = TranslatorFactory.getTranslator(annotation.translator.java)
        serializer.setTranslator(t)
        // 传递上下文给下级处理器，本级不处理
        t.property(property)
        return serializer
    }

    override fun serialize(value: Any?, gen: JsonGenerator, serializers: SerializerProvider) {
        val t = translator
        if (value == null) {
            gen.writeNull()
            return
        }
        if (t != null) {
            try {
                gen.writeObject(t.translate(value.toString()))
            } catch (e: Exception) {
                gen.writeObject(value)
            }
        } else {
            gen.writeObject(value)
        }
    }
}