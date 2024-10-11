package org.apereo.cas.heimdall.authorizer;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import lombok.val;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link DefaultResourceAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public class DefaultResourceAuthorizer implements ResourceAuthorizer {
    @Override
    public AuthorizationResult evaluate(final AuthorizationRequest request, final AuthorizableResource resource) {
        val authorized = resource.isEnforceAllPolicies() ? enforceAllPolicies(request, resource) : enforceAnyPolicy(request, resource);
        return authorized ? AuthorizationResult.granted("OK") : AuthorizationResult.denied("Denied");
    }

    protected boolean enforceAnyPolicy(final AuthorizationRequest request, final AuthorizableResource resource) {
        return resource.getPolicies()
            .parallelStream()
            .map(Unchecked.function(policy -> policy.evaluate(resource, request)))
            .allMatch(AuthorizationResult::authorized);
    }

    protected boolean enforceAllPolicies(final AuthorizationRequest request,
                                         final AuthorizableResource resource) {
        return resource.getPolicies()
            .parallelStream()
            .map(Unchecked.function(policy -> policy.evaluate(resource, request)))
            .allMatch(AuthorizationResult::authorized);
    }
}
