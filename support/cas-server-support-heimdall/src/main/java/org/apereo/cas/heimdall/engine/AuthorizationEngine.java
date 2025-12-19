package org.apereo.cas.heimdall.engine;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.AuthorizationResponse;

/**
 * This is {@link AuthorizationEngine}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface AuthorizationEngine {

    /**
     * Authorize authorization response.
     *
     * @param request the request
     * @return the authorization response
     */
    AuthorizationResponse authorize(AuthorizationRequest request);
}
