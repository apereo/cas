package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link RegisteredServiceCacheKey}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class RegisteredServiceCacheKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceCacheKey.class);

    private final String id;
    private final SamlRegisteredService registeredService;

    public RegisteredServiceCacheKey(final SamlRegisteredService registeredService) {
        this.id = buildRegisteredServiceCacheKey(registeredService);
        this.registeredService = registeredService;
    }

    public String getId() {
        return id;
    }

    public SamlRegisteredService getRegisteredService() {
        return registeredService;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RegisteredServiceCacheKey rhs = (RegisteredServiceCacheKey) obj;
        return new EqualsBuilder()
            .append(this.id, rhs.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("registeredService", registeredService)
            .toString();
    }

    /**
     * Build registered service cache key string.
     *
     * @param service the service
     * @return the string
     */
    public static String buildRegisteredServiceCacheKey(final SamlRegisteredService service) {
        final String key = String.valueOf(service.getId()).concat("@").concat(service.getName());
        LOGGER.debug("Determined cache key for service [{}] as [{}]", service.getName(), key);
        final String hashedKey = DigestUtils.sha512(key);
        LOGGER.debug("Hashed service cache key as [{}]", hashedKey);
        return hashedKey;
    }
}
