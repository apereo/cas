package org.apereo.cas.web.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
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

        final Map<String, String> model = new HashMap<>();
        model.put("refreshEndpoint", busProperties.isEnabled() ? "/cas" + configServerProperties.getPrefix() 
                + "/cas/bus/refresh" : "/cas/status/refresh");
        return new ModelAndView("monitoring/viewDashboard", model);
    }
}
