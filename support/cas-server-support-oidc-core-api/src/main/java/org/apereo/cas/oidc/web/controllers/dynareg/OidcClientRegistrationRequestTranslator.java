package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.oidc.dynareg.OidcClientRegistrationRequest;
import org.apereo.cas.services.OidcRegisteredService;

/**
 * This is {@link OidcClientRegistrationRequestTranslator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface OidcClientRegistrationRequestTranslator {
    /**
     * Translate request into a response and store the service.
     *
     * @param registrationRequest the registration request
     * @param givenService        the given service
     * @return the service
     * @throws Exception the exception
     */
    OidcRegisteredService translate(
        OidcClientRegistrationRequest registrationRequest,
        Optional<OidcRegisteredService> givenService) throws Exception;
}
