package org.apereo.cas.discovery;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasServerDiscoveryProfileEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Endpoint(id = "discovery-profile")
public class CasServerDiscoveryProfileEndpoint extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;

    private final CasServerProfileRegistrar casServerProfileRegistrar;

    /**
     * Instantiates a new mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param casProperties             the cas properties
     * @param servicesManager           the services manager
     * @param casServerProfileRegistrar the cas server profile registrar
     */
    public CasServerDiscoveryProfileEndpoint(final CasConfigurationProperties casProperties,
                                             final ServicesManager servicesManager,
                                             final CasServerProfileRegistrar casServerProfileRegistrar) {
        super(casProperties);
        this.servicesManager = servicesManager;
        this.casServerProfileRegistrar = casServerProfileRegistrar;
    }

    /**
     * Discovery.
     *
     * @param request  the request
     * @param response the response
     * @return the map
     */
    @GetMapping
    @ResponseBody
    @ReadOperation
    public Map<String, Object> discovery(final HttpServletRequest request, final HttpServletResponse response) {
        final Map<String, Object> results = new LinkedHashMap<>();
        results.put("profile", casServerProfileRegistrar.getProfile());
        return results;
    }
}
