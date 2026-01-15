package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.feature.CasRuntimeModule;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * This is {@link CasRuntimeModulesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Endpoint(id = "casModules", defaultAccess = Access.NONE)
public class CasRuntimeModulesEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<@NonNull CasRuntimeModuleLoader> loader;

    public CasRuntimeModulesEndpoint(final CasConfigurationProperties casProperties,
                                     final ObjectProvider<@NonNull CasRuntimeModuleLoader> loader) {
        super(casProperties);
        this.loader = loader;
    }

    /**
     * Report modules.
     *
     * @return the list
     * @throws Exception the exception
     */
    @ReadOperation
    @Operation(summary = "Get all available CAS runtime module descriptors")
    public List<CasRuntimeModule> reportModules() throws Exception {
        return loader.getObject().load();
    }
}
