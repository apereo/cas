package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Set;

/**
 * This is {@link CasFeaturesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Endpoint(id = "casFeatures", enableByDefault = false)
public class CasFeaturesEndpoint extends BaseCasActuatorEndpoint {

    public CasFeaturesEndpoint(final CasConfigurationProperties casProperties) {
        super(casProperties);
    }

    /**
     * Report features.
     *
     * @return the list
     * @throws Exception the exception
     */
    @ReadOperation
    @Operation(summary = "Get all present and registered CAS features")
    public Set<String> features() throws Exception {
        return CasFeatureModule.FeatureCatalog.getRegisteredFeatures();
    }
}
