package org.apereo.cas.heimdall.engine;

import org.apereo.cas.authentication.principal.Principal;

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
     * @param authorizationHeader the authorization header
     * @return the principal
     * @throws Throwable the throwable
     */
    Principal parse(String authorizationHeader) throws Throwable;
}
