package org.apereo.cas.heimdall.engine;

import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.AuthorizationResponse;
import org.apereo.cas.heimdall.authorizer.ResourceAuthorizer;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
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
        val resource = repository.find(request);
        if (resource.isEmpty()) {
            return AuthorizationResponse.notFound("Resource not found");
        }
        for (val authorizer : authorizers) {
            val result = authorizer.evaluate(request, resource.get());
            if (!result.authorized()) {
                return AuthorizationResponse.unauthorized(result.reason());
            }
        }
        return AuthorizationResponse.ok();
    }
}
