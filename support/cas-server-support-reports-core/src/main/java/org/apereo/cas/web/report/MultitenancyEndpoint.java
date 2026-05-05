package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This is {@link MultitenancyEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Endpoint(id = "multitenancy", defaultAccess = Access.NONE)
public class MultitenancyEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<TenantExtractor> tenantExtractor;

    public MultitenancyEndpoint(final CasConfigurationProperties casProperties,
                                final ConfigurableApplicationContext applicationContext,
                                final ObjectProvider<TenantExtractor> tenantExtractor) {
        super(casProperties, applicationContext);
        this.tenantExtractor = tenantExtractor;
    }

    /**
     * All tenant definitions.
     *
     * @return the list
     */
    @GetMapping(
        path = "/tenants",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Report registered tenant definitions")
    public List<TenantDefinition> allTenantDefinitions() {
        return tenantExtractor.getObject().getTenantsManager().findTenants();
    }

    /**
     * All tenant definitions.
     *
     * @return the list
     */
    @GetMapping(
        path = "/tenants/{tenantId}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Report registered tenant definition by its id",
        parameters = @Parameter(name = "tenantId", required = true, description = "The tenant definition id", in = ParameterIn.PATH))
    public ResponseEntity<TenantDefinition> tenantDefinition(@PathVariable final String tenantId) {
        return ResponseEntity.of(tenantExtractor.getObject().getTenantsManager().findTenant(tenantId));
    }

    /**
     * Register a tenant definition.
     *
     * @param tenantDefinition the tenant definition to register
     * @return the registered tenant definition
     */
    @PostMapping(
        path = "/tenants",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            MEDIA_TYPE_SPRING_BOOT_V2_JSON,
            MEDIA_TYPE_SPRING_BOOT_V3_JSON,
            MEDIA_TYPE_CAS_YAML
        })
    @Operation(summary = "Register or update a tenant definition",
        parameters = @Parameter(name = "tenantDefinition", required = true, description = "The tenant definition to register"))
    public ResponseEntity<TenantDefinition> registerTenantDefinition(@RequestBody final TenantDefinition tenantDefinition) {
        val saved = tenantExtractor.getObject().getTenantsManager().save(tenantDefinition);
        return ResponseEntity.ok(saved);
    }
}
