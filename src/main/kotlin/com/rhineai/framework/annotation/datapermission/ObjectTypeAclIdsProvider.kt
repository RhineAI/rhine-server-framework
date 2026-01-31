package com.rhineai.framework.annotation.datapermission

import com.rhine.framework.util.RedisUtil
import jakarta.persistence.Table
import org.springframework.stereotype.Component

/**
 * ACL Provider based on object_type (table name) per entity.
 *
 * Redis key schema: `dp:{userId}:{objectType}`
 * - objectType prefers `schema.table` when schema/catalog present, otherwise `table`
 * - To ensure backward compatibility and cross‑DB support, we lookup multiple candidates:
 *   1) `table`
 *   2) `schema.table` (if schema present)
 *   3) `catalog.schema.table` (if both present)
 *   Additionally, we try lowercase/uppercase variants for each candidate.
 *
 * Value type: Redis Set of allowed entity IDs (as strings).
 */
@Component
class ObjectTypeAclIdsProvider(
    private val redisUtil: RedisUtil
) : AclIdsProvider {
    override fun supports(filterName: String): Boolean = true

    override fun resolveIds(
        filterName: String,
        entityClass: Class<*>,
        userId: String,
        tenantId: String?,
        objectTypeOverride: String?
    ): Collection<*> {
        val table = entityClass.getAnnotation(Table::class.java)
        val name = objectTypeOverride?.takeIf { it.isNotBlank() }
            ?: table?.name?.takeIf { it.isNotBlank() } ?: entityClass.simpleName
        val schema = table?.schema?.takeIf { it.isNotBlank() }
        val catalog = table?.catalog?.takeIf { it.isNotBlank() }

        fun objType(objectType: String) = "dp:$userId:$objectType"

        val candidates = mutableListOf<String>()

        // base: table
        candidates.add(objType(name))
        candidates.add(objType(name.lowercase()))
        candidates.add(objType(name.uppercase()))

        // schema.table
        if (schema != null) {
            val withSchema = "$schema.$name"
            candidates.add(objType(withSchema))
            candidates.add(objType(withSchema.lowercase()))
            candidates.add(objType(withSchema.uppercase()))
        }

        // catalog.schema.table or catalog.table
        if (catalog != null && schema != null) {
            val withCatalogSchema = "$catalog.$schema.$name"
            candidates.add(objType(withCatalogSchema))
            candidates.add(objType(withCatalogSchema.lowercase()))
            candidates.add(objType(withCatalogSchema.uppercase()))
        } else if (catalog != null) {
            val withCatalog = "$catalog.$name"
            candidates.add(objType(withCatalog))
            candidates.add(objType(withCatalog.lowercase()))
            candidates.add(objType(withCatalog.uppercase()))
        }

        val uniqueCandidates = candidates.distinct()
        val merged = mutableSetOf<String>()
        for (key in uniqueCandidates) {
            val members = redisUtil.setMembers(key) ?: emptySet<Any>()
            for (m in members) merged.add(m.toString())
        }

        // Fallback: match any schema/catalog prefixes ending with .<table>
        val suffixes = listOf(name, name.lowercase(), name.uppercase())
        suffixes.distinct().forEach { baseName ->
            val pattern = "dp:$userId:*.$baseName"
            runCatching { redisUtil.keys(pattern) }.getOrDefault(emptySet()).forEach { k ->
                val members = redisUtil.setMembers(k) ?: emptySet<Any>()
                for (m in members) merged.add(m.toString())
            }
        }
        return merged
    }
}