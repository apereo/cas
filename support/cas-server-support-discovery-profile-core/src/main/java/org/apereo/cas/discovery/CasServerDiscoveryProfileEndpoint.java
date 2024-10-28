package org.apereo.cas.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasServerDiscoveryProfileEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "discoveryProfile", defaultAccess = Access.NONE)
public class CasServerDiscoveryProfileEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<CasServerProfileRegistrar> casServerProfileRegistrar;

    public CasServerDiscoveryProfileEndpoint(final CasConfigurationProperties casProperties,
                                             final ObjectProvider<CasServerProfileRegistrar> casServerProfileRegistrar) {
        super(casProperties);
        this.casServerProfileRegistrar = casServerProfileRegistrar;
    }

    /**
     * Discovery.
     *
     * @return the map
     */
    @ReadOperation
    @Operation(summary = "Produce CAS discovery profile")
    public Map<String, Object> discovery() {
        val results = new HashMap<String, Object>();
        results.put("profile", casServerProfileRegistrar.getObject().getProfile());
        return results;
    }
}
