package org.apereo.cas.heimdall.authorizer;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;

/**
 * This is {@link DefaultResourceAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class DefaultResourceAuthorizer implements ResourceAuthorizer {
    @Override
    public AuthorizationResult evaluate(final AuthorizationRequest request, final AuthorizableResource resource) {
        if (resource.getPolicies().stream()
            .map(policy -> policy.evaluate(resource, request))
            .anyMatch(AuthorizationResult::authorized)) {
            return AuthorizationResult.granted("OK");
        }
        return AuthorizationResult.denied("Denied");
    }
}
