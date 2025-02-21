package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

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
}
