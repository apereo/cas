package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import org.apache.cxf.ws.security.tokenstore.SecurityToken;

import java.util.Optional;

/**
 * This is {@link SecurityTokenServiceTokenFetcher}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface SecurityTokenServiceTokenFetcher {
    /**
     * Fetch security token.
     *
     * @param service     the service
     * @param principalId the principal id
     * @return the optional
     */
    Optional<SecurityToken> fetch(Service service, String principalId);
}
