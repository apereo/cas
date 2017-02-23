package org.apereo.cas.web.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.AbstractNamedMvcEndpoint;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at {@code /status/config}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ConfigurationStateController extends AbstractNamedMvcEndpoint {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    @Autowired(required = false)
    private BusProperties busProperties;

    @Autowired
    private ConfigServerProperties configServerProperties;

    public ConfigurationStateController() {
        super("configstate", "/config", true, true);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        final Map<String, Object> model = new HashMap<>();
        final String path = request.getContextPath();
        ControllerUtils.configureModelMapForConfigServerCloudBusEndpoints(busProperties, configServerProperties, path, model);
        return new ModelAndView(VIEW_CONFIG, model);
    }

    /**
     * Update configuration map.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     * @return the map
     */
    @PostMapping("/updateConfiguration")
    @ResponseBody
    protected Map<String, Object> updateConfiguration(@RequestBody final Map jsonInput,
                                                      final HttpServletRequest request,
                                                      final HttpServletResponse response) {
        
        return new HashMap<>();
    }
}
