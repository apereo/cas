package org.apereo.cas.support.saml.services.idp.metadata.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;

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

    public SamlRegisteredServiceCacheKey(final SamlRegisteredService registeredService) {
        this.id = buildRegisteredServiceCacheKey(registeredService);
        this.registeredService = registeredService;
    }

    /**
     * Build registered service cache key string.
     *
     * @param service the service
     * @return the string
     */
    public static String buildRegisteredServiceCacheKey(final SamlRegisteredService service) {
        final var key = String.valueOf(service.getId()).concat("@").concat(service.getName());
        LOGGER.debug("Determined cache key for service [{}] as [{}]", service.getName(), key);
        final var hashedKey = DigestUtils.sha512(key);
        LOGGER.debug("Hashed service cache key as [{}]", hashedKey);
        return hashedKey;
    }
}
