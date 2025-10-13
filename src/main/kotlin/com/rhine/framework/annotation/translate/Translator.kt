package com.rhine.framework.annotation.translate

import com.fasterxml.jackson.databind.BeanProperty

interface Translator<T> {
    fun property(property: BeanProperty) { /* default empty */ }
    fun translate(ids: String): T
}