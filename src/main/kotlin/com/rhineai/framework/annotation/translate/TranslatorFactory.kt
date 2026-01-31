package com.rhineai.framework.annotation.translate

import java.util.concurrent.ConcurrentHashMap

object TranslatorFactory {
    private val map = ConcurrentHashMap<Class<*>, Translator<*>>()

    @Suppress("UNCHECKED_CAST")
    fun getTranslator(clazz: Class<out Translator<*>>): Translator<Any> {
        return map.computeIfAbsent(clazz) {
            try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (e: Exception) {
                e.printStackTrace()
                throw UnsupportedOperationException(e.message, e)
            }
        } as Translator<Any>
    }
}