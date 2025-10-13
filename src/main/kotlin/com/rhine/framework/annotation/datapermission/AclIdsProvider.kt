package com.rhine.framework.annotation.datapermission

/**
 * SPI for resolving accessible entity IDs for the current user (and tenant if enabled).
 * Implementations can query ACL tables, role-resource mappings, etc.
 */
interface AclIdsProvider {
    /**
     * Whether this provider supports the given filter name. Return true to accept all.
     */
    fun supports(filterName: String): Boolean = true

    /**
     * Resolve accessible IDs for the given entity type.
     * @param filterName the Hibernate filter name in use
     * @param entityClass the domain class being accessed
     * @param userId the current user id (never blank)
     * @param tenantId the current tenant id when multi-tenant is enabled, otherwise null
     */
    fun resolveIds(filterName: String, entityClass: Class<*>, userId: String, tenantId: String?): Collection<*>
}

/**
 * Default provider that returns an empty list. If provider mode is enabled without a bean,
 * the aspect will throw to avoid misconfiguration.
 */
class NoopAclIdsProvider : AclIdsProvider {
    override fun resolveIds(filterName: String, entityClass: Class<*>, userId: String, tenantId: String?): Collection<*> = emptyList<Any>()
}