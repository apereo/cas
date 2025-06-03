package org.apereo.cas.heimdall.engine;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.heimdall.AuthorizationRequest;

/**
 * This is {@link AuthorizationPrincipalParser}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface AuthorizationPrincipalParser {
    /**
     * Parse principal.
     *
     * @param authorizationHeader  the authorization header
     * @param authorizationRequest the authorization request
     * @return the principal
     * @throws Throwable the throwable
     */
    Principal parse(String authorizationHeader, AuthorizationRequest authorizationRequest) throws Throwable;
}
