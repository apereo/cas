package org.apereo.cas.heimdall;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

/**
 * This is {@link HeimdallAuthorizationEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@Endpoint(id = "heimdall", defaultAccess = Access.NONE)
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
     */
    @PostMapping(path = "/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch authorizable resource matching the given authorization request in the body",
        parameters = @Parameter(name = "request", required = true, description = "Authorization request in the body"))
    public ResponseEntity<AuthorizableResource> fetchResource(@RequestBody final AuthorizationRequest request) {
        return ResponseEntity.of(authorizableResourceRepository.find(request));
    }

    /**
     * Fetch resources.
     *
     * @return the response entity
     */
    @GetMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch all authorizable resources")
    public ResponseEntity<Map<String, List<AuthorizableResource>>> fetchResources() {
        val resources = authorizableResourceRepository.findAll();
        return ResponseEntity.ok(resources);
    }

    /**
     * Fetch resources for namespace.
     *
     * @param namespace the namespace
     * @return the response entity
     */
    @GetMapping(path = "/resources/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch all authorizable resources for namespace",
        parameters = @Parameter(name = "namespace", in = ParameterIn.PATH, required = true, description = "Namespace to fetch resources for"))
    public ResponseEntity<List<AuthorizableResource>> fetchResourcesForNamespace(
        @PathVariable final String namespace) {
        val resources = authorizableResourceRepository.find(namespace);
        return ResponseEntity.ok(resources);
    }

    /**
     * Fetch resources for namespace by id.
     *
     * @param namespace the namespace
     * @param id        the id
     * @return the response entity
     */
    @GetMapping(path = "/resources/{namespace}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch all authorizable resources for namespace by id",
        parameters = {
            @Parameter(name = "namespace", in = ParameterIn.PATH, required = true, description = "Namespace to fetch resources for"),
            @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "Resource id to fetch")
        })
    public ResponseEntity<AuthorizableResource> fetchResourcesForNamespaceById(
        @PathVariable final String namespace, @PathVariable final long id) {
        val resource = authorizableResourceRepository.find(namespace, id);
        return ResponseEntity.of(resource);
    }

}
