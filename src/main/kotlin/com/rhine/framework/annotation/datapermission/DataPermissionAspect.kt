package com.rhine.framework.annotation.datapermission

import com.rhine.framework.exception.NoPermissionException
import jakarta.persistence.EntityManager
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.hibernate.Session
// Removed direct import of SecurityContextHolder to avoid hard dependency
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.lang.reflect.ParameterizedType

/**
 * Data permission aspect that enables a Hibernate filter for the current session
 * using the current user/tenant from header or Spring Security principal.
 *
 * Works with annotations placed on:
 * - Repository methods
 * - Repository interfaces
 * - Service/controller classes or methods
 */
@Aspect
class DataPermissionAspect(
    private val entityManager: EntityManager,
    private val properties: DataPermissionProperties = DataPermissionProperties(),
    private val providers: List<AclIdsProvider> = emptyList()
) {

    // Intercept Spring Data repositories
    @Pointcut("execution(* org.springframework.data.repository.Repository+.*(..))")
    fun repositoryMethods() {}

    // Also allow explicit annotation-based interception on services/controllers
    @Pointcut("@within(com.rhine.framework.annotation.datapermission.DataPermission) || @annotation(com.rhine.framework.annotation.datapermission.DataPermission)")
    fun annotatedTypesOrMethods() {}

    @Around("repositoryMethods() || annotatedTypesOrMethods()")
    fun aroundInvocation(pjp: ProceedingJoinPoint): Any? {
        val permission = resolveAnnotation(pjp) ?: return pjp.proceed()

        // Merge annotation with properties defaults
        val useSecurity = permission.useSecurity || properties.useSecurity
        val filterName = permission.filterName.ifBlank { properties.filterName }
        val multiTenant = permission.multiTenant || properties.multiTenant
        val userHeader = permission.userHeader.ifBlank { properties.userHeader }
        val tenantHeader = permission.tenantHeader.ifBlank { properties.tenantHeader }
        val userParamName = permission.userParamName.ifBlank { properties.userParamName }
        val tenantParamName = permission.tenantParamName.ifBlank { properties.tenantParamName }
        val useProvider = permission.useProvider || properties.useProvider
        val providerParamName = permission.providerParamName.ifBlank { properties.providerParamName }

        val (userId, tenantId) = if (useSecurity) {
            extractFromSecurity(multiTenant)
        } else {
            extractFromHeaders(userHeader, tenantHeader, multiTenant)
        }

        val session = entityManager.unwrap(Session::class.java)
        val alreadyEnabled = session.getEnabledFilter(filterName) != null
        if (!alreadyEnabled) {
            val filter = session.enableFilter(filterName)
                ?: throw IllegalStateException("Filter '$filterName' is not defined on any of the participating entities")

            if (useProvider) {
                val targetClass = resolveTargetDomainClass(pjp)
                val provider = resolveProvider(filterName)
                val ids = provider.resolveIds(filterName, targetClass, userId, if (multiTenant) tenantId else null)
                @Suppress("UNCHECKED_CAST")
                filter.setParameter(providerParamName, ids)
                if (multiTenant) {
                    // Provider-mode filters may also include tenant condition when multi-tenant is enabled
                    filter.setParameter(tenantParamName, tenantId ?: throw NoPermissionException())
                }
            } else {
                filter.setParameter(userParamName, userId)
                if (multiTenant) {
                    filter.setParameter(tenantParamName, tenantId ?: throw NoPermissionException())
                }
            }
        }
        try {
            return pjp.proceed()
        } finally {
            if (!alreadyEnabled) {
                session.disableFilter(filterName)
            }
        }
    }

    private fun resolveAnnotation(joinPoint: JoinPoint): DataPermission? {
        val sig = joinPoint.signature as? MethodSignature ?: return null
        // 1) method-level on the invoked method (works for JDK proxy methods)
        sig.method.getAnnotation(DataPermission::class.java)?.let { return it }
        val targetClass = (joinPoint as? ProceedingJoinPoint)?.target?.javaClass ?: return null
        // 2) class-level on target class
        targetClass.getAnnotation(DataPermission::class.java)?.let { return it }
        // 3) interface-level on repository interface or its methods
        for (intf in targetClass.interfaces) {
            intf.getAnnotation(DataPermission::class.java)?.let { return it }
            try {
                val intfMethod = intf.getMethod(sig.method.name, *sig.method.parameterTypes)
                intfMethod.getAnnotation(DataPermission::class.java)?.let { return it }
            } catch (_: NoSuchMethodException) {
                // ignore and continue
            }
        }
        return null
    }

    private fun extractFromHeaders(userHeader: String, tenantHeader: String, multiTenant: Boolean): Pair<String, String?> {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw NoPermissionException()
        val userId = attrs.request.getHeader(userHeader)
        if (userId.isNullOrBlank()) throw NoPermissionException()
        val tenantId = if (multiTenant) attrs.request.getHeader(tenantHeader) else null
        if (multiTenant && tenantId.isNullOrBlank()) throw NoPermissionException()
        return Pair(userId, tenantId)
    }

    private fun extractFromSecurity(multiTenant: Boolean): Pair<String, String?> {
        // Resolve SecurityContextHolder via reflection to avoid mandatory dependency
        val ctxHolderClass = runCatching {
            Class.forName("org.springframework.security.core.context.SecurityContextHolder")
        }.getOrNull() ?: throw NoPermissionException()
        val context = runCatching {
            val getContext = ctxHolderClass.getMethod("getContext")
            getContext.invoke(null)
        }.getOrNull() ?: throw NoPermissionException()
        val authentication = runCatching {
            context.javaClass.getMethod("getAuthentication").invoke(context)
        }.getOrNull() ?: throw NoPermissionException()
        val principal = runCatching {
            authentication.javaClass.getMethod("getPrincipal").invoke(authentication)
        }.getOrNull() ?: throw NoPermissionException()

        val userId: String = when (principal) {
            is String -> principal
            else -> runCatching { principal.javaClass.getMethod("getUsername").invoke(principal) as? String }.getOrNull()
                ?: runCatching { principal.javaClass.getMethod("getUserId").invoke(principal) as? String }.getOrNull()
                ?: runCatching { principal.javaClass.getMethod("getId").invoke(principal) as? String }.getOrNull()
                ?: throw NoPermissionException()
        }

        val tenantId: String? = if (multiTenant) {
            val details = runCatching { authentication.javaClass.getMethod("getDetails").invoke(authentication) }.getOrNull()
            runCatching { principal.javaClass.getMethod("getTenantId").invoke(principal) as? String }.getOrNull()
                ?: runCatching { details?.javaClass?.getMethod("getTenantId")?.invoke(details) as? String }.getOrNull()
                ?: throw NoPermissionException()
        } else null

        return Pair(userId, tenantId)
    }

    private fun resolveProvider(filterName: String): AclIdsProvider {
        if (providers.isEmpty()) throw IllegalStateException("No AclIdsProvider bean found while provider mode is enabled for filter '$filterName'")
        val matched = providers.firstOrNull { runCatching { it.supports(filterName) }.getOrDefault(true) }
        return matched ?: providers.first()
    }

    private fun resolveTargetDomainClass(pjp: ProceedingJoinPoint): Class<*> {
        val targetClass = pjp.target.javaClass
        // Try to resolve Repository<T, ID> generic argument as domain class
        for (intf in targetClass.genericInterfaces) {
            val type = intf
            if (type is ParameterizedType) {
                val raw = type.rawType as? Class<*> ?: continue
                if (org.springframework.data.repository.Repository::class.java.isAssignableFrom(raw)) {
                    val args = type.actualTypeArguments
                    val domain = args.firstOrNull()
                    val domainClass = when (domain) {
                        is Class<*> -> domain
                        is ParameterizedType -> domain.rawType as? Class<*>
                        else -> null
                    }
                    if (domainClass != null) return domainClass
                }
            }
        }
        return targetClass
    }
}