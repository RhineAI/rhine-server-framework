package com.rhine.framework.annotation.datapermission

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = DataPermissionProperties.PREFIX)
class DataPermissionProperties {
    companion object { const val PREFIX = "framework.data-permission" }

    var enabled: Boolean = true
    var filterName: String = "data_permission_filter"
    var useSecurity: Boolean = false
    var multiTenant: Boolean = false

    var userHeader: String = "currentUserId"
    var tenantHeader: String = "currentTenantId"

    var userParamName: String = "userId"
    var tenantParamName: String = "tenantId"

    // Provider (SPI) mode: when enabled, the aspect will fetch accessible IDs via AclIdsProvider
    var useProvider: Boolean = false
    var providerParamName: String = "aclIds"
}