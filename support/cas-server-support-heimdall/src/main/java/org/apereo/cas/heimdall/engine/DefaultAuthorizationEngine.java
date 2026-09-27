package org.apereo.cas.heimdall.engine;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.AuthorizationResponse;
import org.apereo.cas.heimdall.authorizer.ResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link DefaultAuthorizationEngine}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
public class DefaultAuthorizationEngine implements AuthorizationEngine {
    private final AuthorizableResourceRepository repository;
    private final List<ResourceAuthorizer> authorizers;

    @Override
    public AuthorizationResponse authorize(final AuthorizationRequest request) {
        if (request.getPrincipal() == null) {
            return AuthorizationResponse.unauthorized("No authorization principal could be resolved");
        }
        val resources = findResources(request);
        if (resources.isEmpty()) {
            return AuthorizationResponse.notFound("No authorizable resource can be found");
        }

        for (val resource : resources) {
            for (val authorizer : authorizers) {
                val result = authorizer.evaluate(request, resource);
                if (!result.authorized()) {
                    return AuthorizationResponse.unauthorized(result.reason());
                }
            }
        }
        return AuthorizationResponse.ok();
    }

    private List<AuthorizableResource> findResources(final AuthorizationRequest request) {
        if (request.isAuthZen()) {
            return repository.find(request.getResource(), request.getAction());
        }
        return repository.find(request).map(List::of).orElseGet(List::of);
    }
}
