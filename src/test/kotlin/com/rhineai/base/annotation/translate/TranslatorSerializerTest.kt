package com.rhine.framework.annotation.translate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TranslatorSerializerTest {

    data class Dto(
        @Translate(translator = EchoTranslator::class)
        val name: String
    )

    class EchoTranslator : Translator<String> {
        override fun property(property: com.fasterxml.jackson.databind.BeanProperty) { /* no-op */ }
        override fun translate(ids: String): String = "TR-" + ids
    }

    @Test
    fun `translate serializer is applied to field`() {
        val om = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val dto = Dto(name = "abc")
        val json = om.writeValueAsString(dto)
        // expect {"name":"TR-abc"}
        assertEquals("{\"name\":\"TR-abc\"}", json)
    }
}