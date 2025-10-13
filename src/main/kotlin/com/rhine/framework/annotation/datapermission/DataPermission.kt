package com.rhine.framework.annotation.datapermission

/**
 * Marks a class or method to enable data permission filtering for the duration of the invocation.
 * A Hibernate filter defined on your entities should be used to actually restrict records.
 *
 * Usage guidance:
 * - Define a Hibernate @FilterDef and @Filter on your entity using the same filterName.
 * - The filter condition should compare the designated column to parameters like :userId or :tenantId.
 * - This aspect will read identifiers from either a request header or Spring Security context.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataPermission(
    /**
     * Hibernate filter name declared on your entities with @Filter(name = ...).
     */
    val filterName: String = "data_permission_filter",

    /**
     * Whether to read the current user (and tenant if enabled) from Spring Security's SecurityContext.
     * If false, headers will be used.
     */
    val useSecurity: Boolean = false,

    /**
     * Request header name for current user id when useSecurity=false.
     */
    val userHeader: String = "currentUserId",

    /**
     * Enable multi-tenant filtering. When enabled, the filter is expected to accept a :tenantId parameter.
     */
    val multiTenant: Boolean = false,

    /**
     * Request header name for current tenant id when useSecurity=false and multiTenant=true.
     */
    val tenantHeader: String = "currentTenantId",

    /**
     * Filter parameter name for user id (default 'userId').
     */
    val userParamName: String = "userId",

    /**
     * Filter parameter name for tenant id (default 'tenantId'). Used only when multiTenant=true.
     */
    val tenantParamName: String = "tenantId",

    /**
     * Enable provider (SPI) mode. When true, the aspect will use an AclIdsProvider to resolve accessible IDs
     * for the current user (and tenant if enabled), and inject them into the Filter as a parameter list.
     */
    val useProvider: Boolean = false,

    /**
     * Parameter name to pass the ID list resolved by provider mode (default 'aclIds').
     */
    val providerParamName: String = "aclIds"
)