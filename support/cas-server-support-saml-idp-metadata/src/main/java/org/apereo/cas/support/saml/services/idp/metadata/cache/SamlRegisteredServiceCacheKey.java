package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.criterion.EntityIdCriterion;

import java.io.Serializable;
import java.util.Objects;

/**
 * This is {@link SamlRegisteredServiceCacheKey}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class SamlRegisteredServiceCacheKey implements Serializable {
    /**
     * Cache key field separator.
     */
    static final String KEY_SEPARATOR = "|";

    private static final long serialVersionUID = -7238573226470492601L;
    
    private final String id;

    private final SamlRegisteredService registeredService;

    private final transient CriteriaSet criteriaSet;

    @Getter(AccessLevel.PACKAGE)
    private final String cacheKey;

    public SamlRegisteredServiceCacheKey(final SamlRegisteredService registeredService,
                                         final CriteriaSet criteriaSet) {
        this.cacheKey = getCacheKeyForRegisteredService(registeredService, criteriaSet);
        LOGGER.trace("Calculated service cache key [{}]", cacheKey);
        this.id = buildRegisteredServiceCacheKey(this.cacheKey);
        this.registeredService = registeredService;
        this.criteriaSet = criteriaSet;
    }
    
    private static String buildRegisteredServiceCacheKey(final String key) {
        val hashedKey = DigestUtils.sha512(key);
        LOGGER.trace("Hashed service cache key [{}] as [{}]", key, hashedKey);
        return hashedKey;
    }

    private static String getCacheKeyForRegisteredService(final SamlRegisteredService service,
                                                          final CriteriaSet criteriaSet) {
        val entityId = criteriaSet.contains(EntityIdCriterion.class)
            ? Objects.requireNonNull(criteriaSet.get(EntityIdCriterion.class)).getEntityId()
            : service.getServiceId();
        if (SamlUtils.isDynamicMetadataQueryConfigured(service.getMetadataLocation())) {
            return entityId;
        }
        return service.getMetadataLocation();
    }
}
