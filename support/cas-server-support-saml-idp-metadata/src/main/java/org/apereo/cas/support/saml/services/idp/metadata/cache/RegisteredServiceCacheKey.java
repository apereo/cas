package org.apereo.cas.support.saml.services.idp.metadata.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;

/**
 * This is {@link RegisteredServiceCacheKey}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class RegisteredServiceCacheKey {
    private final String id;
    private final SamlRegisteredService registeredService;

    public RegisteredServiceCacheKey(final SamlRegisteredService registeredService) {
        this.id = buildRegisteredServiceCacheKey(registeredService);
        this.registeredService = registeredService;
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
