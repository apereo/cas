package org.apereo.cas.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasServerDiscoveryProfileEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Endpoint(id = "discoveryProfile", defaultAccess = Access.NONE)
public class CasServerDiscoveryProfileEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<CasServerProfileRegistrar> casServerProfileRegistrar;

    public CasServerDiscoveryProfileEndpoint(final CasConfigurationProperties casProperties,
                                             final ConfigurableApplicationContext applicationContext,
                                             final ObjectProvider<CasServerProfileRegistrar> casServerProfileRegistrar) {
        super(casProperties, applicationContext);
        this.casServerProfileRegistrar = casServerProfileRegistrar;
    }

    /**
     * Discovery.
     *
     * @return the map
     */
    @GetMapping
    @Operation(summary = "Produce CAS discovery profile")
    public Map<String, Object> discovery(final HttpServletRequest request, final HttpServletResponse response) {
        val results = new HashMap<String, Object>();
        results.put("profile", casServerProfileRegistrar.getObject().getProfile(request, response));
        return results;
    }
}
