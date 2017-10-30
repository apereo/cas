package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasServerDiscoveryProfileController}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasServerDiscoveryProfileController extends BaseCasMvcEndpoint {
    private final ServicesManager servicesManager;
    
    private final CasConfigurationProperties casProperties;
    
    private final CasServerProfileRegistrar casServerProfileRegistrar;

    /**
     * Instantiates a new mvc endpoint.
     * Endpoints are by default sensitive.
     *
     * @param casProperties             the cas properties
     * @param servicesManager           the services manager
     * @param casServerProfileRegistrar the cas server profile registrar
     */
    public CasServerDiscoveryProfileController(final CasConfigurationProperties casProperties,
                                               final ServicesManager servicesManager,
                                               final CasServerProfileRegistrar casServerProfileRegistrar) {
        super("casdiscovery", "/discovery", casProperties.getMonitor().getEndpoints().getDiscovery(), casProperties);
        this.servicesManager = servicesManager;
        this.casProperties = casProperties;
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
    public Map<String, Object> discovery(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        
        final Map<String, Object> results = new LinkedHashMap<>();
        results.put("profile", casServerProfileRegistrar.getProfile());
        return results;
    }
}
