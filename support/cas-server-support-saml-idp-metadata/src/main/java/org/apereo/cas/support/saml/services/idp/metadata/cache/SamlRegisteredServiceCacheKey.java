package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

import java.io.Serializable;

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
    private static final long serialVersionUID = -7238573226470492601L;

    private final String id;
    private final SamlRegisteredService registeredService;
    private final transient CriteriaSet criteriaSet;

    public SamlRegisteredServiceCacheKey(final SamlRegisteredService registeredService,
                                         final CriteriaSet criteriaSet) {
        this.id = buildRegisteredServiceCacheKey(registeredService);
        this.registeredService = registeredService;
        this.criteriaSet = criteriaSet;
    }

    /**
     * Build registered service cache key string.
     *
     * @param service the service
     * @return the string
     */
    public static String buildRegisteredServiceCacheKey(final SamlRegisteredService service) {
        val key = SamlUtils.isDynamicMetadataQueryConfigured(service.getMetadataLocation())
                ? service.getServiceId()
                : service.getMetadataLocation();
        LOGGER.trace("Determined cache key for service [{}] as [{}]", service.getName(), key);
        val hashedKey = DigestUtils.sha512(key);
        LOGGER.trace("Hashed service cache key as [{}]", hashedKey);
        return hashedKey;
    }
}
