package org.apereo.cas.heimdall.engine;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.AuthorizationResponse;
import org.apereo.cas.heimdall.authorizer.ResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.List;

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
        if (StringUtils.isBlank(request.getNamespace())) {
            return repository.find(request.getResource().getId());
        }
        val resource = repository.find(request);
        return resource.map(List::of).orElseGet(List::of);
    }
}
