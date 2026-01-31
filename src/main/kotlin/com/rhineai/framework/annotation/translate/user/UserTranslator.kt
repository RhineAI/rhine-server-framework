package com.rhineai.framework.annotation.translate.user

import cn.hutool.json.JSONUtil
import cn.hutool.json.JSONObject
import com.fasterxml.jackson.databind.BeanProperty
import com.rhineai.framework.annotation.translate.Translator
import com.rhineai.framework.entity.vo.translator.user.DataUser
import com.rhineai.framework.redis.BaseRedis
import com.rhineai.framework.spring.SpringContextHolder

class UserTranslator : Translator<List<DataUser>> {
    override fun property(property: BeanProperty) {
    }

    override fun translate(ids: String): List<DataUser> {
        if (ids.isBlank()) return emptyList()
        val baseRedis = SpringContextHolder.getBean(BaseRedis::class.java)
        val userObj = baseRedis.getObject("translate:userMap")
        val userMap = if (userObj is Map<*, *>) userObj else emptyMap<Any, Any>()
        val dictObj = baseRedis.getObject("translate:dict")
        val dictMap = if (dictObj is Map<*, *>) dictObj else emptyMap<Any, Any>()
        val list = ids.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return list.map { item ->
            val raw = userMap[item]
            val dataUser = when (raw) {
                is DataUser -> raw
                is JSONObject -> JSONUtil.toBean(raw, DataUser::class.java)
                is Map<*, *> -> JSONUtil.toBean(JSONUtil.parseObj(raw), DataUser::class.java)
                is String -> runCatching { JSONUtil.toBean(JSONUtil.parseObj(raw), DataUser::class.java) }.getOrNull()
                else -> null
            } ?: run {
                val name = resolveName(dictMap, item)
                DataUser(
                    id = item,
                    name = name ?: item,
                    status = null,
                    navigator = name != null,
                    deleteFlag = if (name == null) 1 else 0
                )
            }
            if (dataUser.status == "1" || dataUser.deleteFlag == 1) {
                dataUser.navigator = false
            }
            dataUser
        }
    }

    private fun resolveName(dictMap: Map<*, *>, id: String): String? {
        val v1 = dictMap["o-flow-user.id:$id"] as? String
        if (!v1.isNullOrBlank()) return v1
        val v2 = dictMap["next-flow-user.id:$id"] as? String
        if (!v2.isNullOrBlank()) return v2
        return dictMap["o-flow-user:$id"] as? String
    }
}
