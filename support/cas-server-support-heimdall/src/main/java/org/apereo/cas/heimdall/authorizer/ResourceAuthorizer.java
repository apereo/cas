package org.apereo.cas.heimdall.authorizer;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;

/**
 * This is {@link ResourceAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface ResourceAuthorizer {
    /**
     * Authorize boolean.
     *
     * @param resource the resource
     * @return the boolean
     */
    AuthorizationResult evaluate(AuthorizationRequest request, AuthorizableResource resource);
}
