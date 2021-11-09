package org.apereo.cas.oidc.issuer;

import org.apereo.cas.services.OidcRegisteredService;

import java.util.Optional;

/**
 * This is {@link OidcIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface OidcIssuerService {

    /**
     * Immutable issuer service that always returns a static issuer.
     *
     * @param issuer the issuer
     * @return the oidc issuer service
     */
    static OidcIssuerService immutable(final String issuer) {
        return registeredService -> issuer;
    }

    /**
     * Determine issuer.
     *
     * @param registeredService the registered service
     * @return the string
     */
    String determineIssuer(Optional<OidcRegisteredService> registeredService);
}
