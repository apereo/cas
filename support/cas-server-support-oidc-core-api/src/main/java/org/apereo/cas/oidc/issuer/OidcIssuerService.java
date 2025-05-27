package org.apereo.cas.oidc.issuer;

import org.apereo.cas.services.OidcRegisteredService;
import org.pac4j.core.context.WebContext;
import java.util.List;
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
     * Default bean name.
     */
    String BEAN_NAME = "oidcIssuerService";

    /**
     * Immutable issuer service that always returns a static issuer.
     *
     * @param issuer the issuer
     * @return the oidc issuer service
     */
    static OidcIssuerService echoing(final String issuer) {
        return registeredService -> issuer;
    }

    /**
     * Determine issuer.
     *
     * @param registeredService the registered service
     * @return the string
     */
    String determineIssuer(Optional<OidcRegisteredService> registeredService);

    /**
     * Is valid issuer for endpoint.
     * This operation should calculate the expected issuer from the request
     * and compare that with the system-defined issuer, while considering
     * the endpoint that is requesting access.
     *
     * @param webContext the web context
     * @param endpoints  the endpoint
     * @return true/false
     */
    default boolean validateIssuer(final WebContext webContext, final List<String> endpoints) {
        return validateIssuer(webContext, endpoints, null);
    }

    /**
     * Is valid issuer for endpoint.
     * This operation should calculate the expected issuer from the request
     * and compare that with the system-defined issuer, while considering
     * the endpoint that is requesting access.
     *
     * @param webContext        the web context
     * @param endpoints         the endpoints
     * @param registeredService the registered service
     * @return the boolean
     */
    default boolean validateIssuer(final WebContext webContext, final List<String> endpoints,
                                   final OidcRegisteredService registeredService) {
        return true;
    }
}
