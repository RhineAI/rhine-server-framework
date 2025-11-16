package com.rhine.framework.spring

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class SpringContextHolder : ApplicationContextAware {
    companion object {
        @JvmStatic
        lateinit var context: ApplicationContext

        @JvmStatic
        fun <T> getBean(clazz: Class<T>): T = context.getBean(clazz)

        @JvmStatic
        fun getBean(name: String): Any = context.getBean(name)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}