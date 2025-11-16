package com.rhine.framework.annotation.translate.dictionary

import com.fasterxml.jackson.databind.BeanProperty
import com.rhine.framework.annotation.translate.Translator
import com.rhine.framework.entity.vo.translator.dictionary.DataDictionary
import com.rhine.framework.redis.BaseRedis
import com.rhine.framework.spring.SpringContextHolder

class DictTranslator : Translator<List<DataDictionary>> {
    private var dictType: String = ""
    private var publicKey: String = ""

    override fun property(property: BeanProperty) {
        val ann = property.getAnnotation(DictTranslate::class.java)
        dictType = ann.dictType
        publicKey = ann.publicKey
    }

    override fun translate(ids: String): List<DataDictionary> {
        if (ids.isEmpty()) return emptyList()
        val baseRedis = SpringContextHolder.getBean(BaseRedis::class.java)
        val obj = baseRedis.getObject("translate:dict")
        val map: Map<*, *> = if (obj is Map<*, *>) obj else emptyMap<String, String>()
        val list = ids.split(",")
        return list.map { item ->
            val primaryKey = dictType + item
            val v1 = map[primaryKey] as? String
            val altType = when {
                dictType.endsWith(".id:") -> dictType.replace(".id:", ":")
                dictType.endsWith(":") -> dictType.dropLast(1) + ".id:"
                else -> dictType
            }
            val v2 = map[altType + item] as? String
            val name = v1 ?: v2 ?: item
            DataDictionary(id = item, name = name)
        }
    }
}