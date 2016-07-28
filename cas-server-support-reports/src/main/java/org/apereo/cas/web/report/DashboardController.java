package org.apereo.cas.web.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.EndpointProperties;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Controller("dashboardController")
public class DashboardController {
    
    @Autowired
    private BusProperties busProperties;
    
    @Autowired
    private ConfigServerProperties configServerProperties;
    
    @Autowired
    private RestartEndpoint restartEndpoint;
    
    @Autowired
    private ShutdownEndpoint shutdownEndpoint;
    
    @Autowired
    private EndpointProperties endpointProperties;
    
    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping("/status/dashboard")
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Map<String, Object> model = new HashMap<>();
        
        final String path = request.getContextPath();
        if (busProperties.isEnabled()) {
            model.put("refreshEndpoint", path + configServerProperties.getPrefix() + "/cas/bus/refresh");
            model.put("refreshMethod", "GET");
        } else {
            model.put("refreshEndpoint", path + "/status/refresh");
            model.put("refreshMethod", "POST");
        }
        model.put("restartEndpointEnabled", restartEndpoint.isEnabled() && endpointProperties.getEnabled());
        model.put("shutdownEndpointEnabled", shutdownEndpoint.isEnabled() && endpointProperties.getEnabled());
        return new ModelAndView("monitoring/viewDashboard", model);
    }
}
