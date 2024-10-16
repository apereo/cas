package org.apereo.cas.heimdall;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link HeimdallAuthorizationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@Endpoint(id = "heimdall", enableByDefault = false)
public class HeimdallAuthorizationEndpoint extends BaseCasRestActuatorEndpoint {
    protected final AuthorizableResourceRepository authorizableResourceRepository;

    public HeimdallAuthorizationEndpoint(final CasConfigurationProperties casProperties,
                                         final ConfigurableApplicationContext applicationContext,
                                         final AuthorizableResourceRepository authorizableResourceRepository) {
        super(casProperties, applicationContext);
        this.authorizableResourceRepository = authorizableResourceRepository;
    }

    /**
     * Fetch resources matching this request.
     *
     * @param request the request
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch authorizable resources matching the given authorization request in the body")
    public ResponseEntity<AuthorizableResource> fetchResources(@RequestBody final AuthorizationRequest request) throws Throwable {
        return ResponseEntity.of(authorizableResourceRepository.find(request));
    }
}
