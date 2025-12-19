package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;

/**
 * This is {@link OidcRegisteredServiceAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface OidcRegisteredServiceAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
    /**
     * Gets allowed claims.
     *
     * @return the allowed attributes
     */
    List<String> getAllowedAttributes();

    /**
     * Gets claim mappings.
     *
     * @return the claim mappings
     */
    Map<String, String> getClaimMappings();

    /**
     * Gets scope type.
     *
     * @return the scope type
     */
    String getScopeType();
}
