package org.apereo.cas.heimdall.authorizer;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link DefaultResourceAuthorizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class DefaultResourceAuthorizer implements ResourceAuthorizer {
    @Override
    public AuthorizationResult evaluate(final AuthorizationRequest request, final AuthorizableResource resource) {
        if (resource.getPolicies().isEmpty()) {
            return AuthorizationResult.denied("No authorization policies are defined for resource " + resource.getId());
        }
        val authorized = resource.isEnforceAllPolicies() ? enforceAllPolicies(request, resource) : enforceAnyPolicy(request, resource);
        return authorized ? AuthorizationResult.granted("OK") : AuthorizationResult.denied("Denied");
    }

    protected boolean enforceAnyPolicy(final AuthorizationRequest request, final AuthorizableResource resource) {
        val failures = new ArrayList<Throwable>();
        for (val policy : resource.getPolicies()) {
            try {
                if (policy.evaluate(resource, request).authorized()) {
                    return true;
                }
            } catch (final Throwable e) {
                if (e instanceof final Error error) {
                    throw error;
                }
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                LOGGER.warn("Authorization policy [{}] for resource [{}] failed: [{}]",
                    policy.getClass().getSimpleName(), resource.getId(), e.getMessage());
                failures.add(e);
            }
        }
        if (!failures.isEmpty()) {
            val exception = new IllegalStateException("No authorization policy granted access to resource %s and %s failed"
                .formatted(resource.getId(), failures.size()), failures.getFirst());
            failures.stream().skip(1).forEach(exception::addSuppressed);
            throw exception;
        }
        return false;
    }

    protected boolean enforceAllPolicies(final AuthorizationRequest request,
                                         final AuthorizableResource resource) {
        return resource.getPolicies()
            .stream()
            .map(Unchecked.function(policy -> policy.evaluate(resource, request)))
            .allMatch(AuthorizationResult::authorized);
    }
}
