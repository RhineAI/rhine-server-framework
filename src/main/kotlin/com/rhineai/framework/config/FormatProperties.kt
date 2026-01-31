package com.rhineai.framework.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = FormatProperties.PREFIX)
class FormatProperties {
    companion object {
        const val PREFIX = "rhine.framework.format"
    }

    /**
     * Whether to convert all Long values to String in response.
     */
    var longToString: Boolean = true

    /**
     * Whether to convert all Date/Time values to timestamp String in response.
     */
    var dateToTimestampString: Boolean = true
}
