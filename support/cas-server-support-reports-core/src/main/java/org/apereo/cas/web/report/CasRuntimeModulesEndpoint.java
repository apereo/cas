package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.feature.CasRuntimeModule;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.List;

/**
 * This is {@link CasRuntimeModulesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Endpoint(id = "casModules", enableByDefault = false)
public class CasRuntimeModulesEndpoint extends BaseCasActuatorEndpoint {
    private final CasRuntimeModuleLoader loader;

    public CasRuntimeModulesEndpoint(final CasConfigurationProperties casProperties, final CasRuntimeModuleLoader loader) {
        super(casProperties);
        this.loader = loader;
    }

    /**
     * Report modules.
     *
     * @return the list
     */
    @ReadOperation
    @Operation(summary = "Get all available CAS runtime module descriptors")
    public List<CasRuntimeModule> reportModules() {
        return loader.load();
    }
}
