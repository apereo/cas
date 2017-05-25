package org.apereo.cas.web.report;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AutoConfigurationReportEndpoint;
import org.springframework.boot.actuate.endpoint.BeansEndpoint;
import org.springframework.boot.actuate.endpoint.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.DumpEndpoint;
import org.springframework.boot.actuate.endpoint.EndpointProperties;
import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.RequestMappingEndpoint;
import org.springframework.boot.actuate.endpoint.ShutdownEndpoint;
import org.springframework.boot.actuate.endpoint.TraceEndpoint;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
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
public class DashboardController extends BaseCasMvcEndpoint {

    @Autowired
    private RestartEndpoint restartEndpoint;

    @Autowired
    private ShutdownEndpoint shutdownEndpoint;

    @Autowired
    private EndpointProperties endpointProperties;

    @Autowired
    private InfoEndpoint infoEndpoint;

    @Autowired
    private AutoConfigurationReportEndpoint autoConfigurationReportEndpoint;

    @Autowired
    private BeansEndpoint beansEndpoint;

    @Autowired
    private DumpEndpoint dumpEndpoint;

    @Autowired
    private ConfigurationPropertiesReportEndpoint configPropertiesEndpoint;

    @Autowired
    private RequestMappingEndpoint requestMappingEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private TraceEndpoint traceEndpoint;

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private ApplicationContext applicationContext;

    private CasConfigurationProperties casProperties;

    public DashboardController(final CasConfigurationProperties casProperties) {
        super("casdashboard", "/dashboard", casProperties.getMonitor().getEndpoints().getDashboard(), casProperties);
        this.casProperties = casProperties;
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> model = new HashMap<>();
        model.put("restartEndpointEnabled", restartEndpoint.isEnabled() && endpointProperties.getEnabled());
        model.put("environmentEndpointEnabled", environmentEndpoint.isEnabled() && endpointProperties.getEnabled());
        model.put("shutdownEndpointEnabled", shutdownEndpoint.isEnabled() && endpointProperties.getEnabled());
        model.put("serverFunctionsEnabled", (Boolean) model.get("restartEndpointEnabled") || (Boolean) model.get("shutdownEndpointEnabled"));

        model.put("autoConfigurationEndpointEnabled", autoConfigurationReportEndpoint.isEnabled());
        model.put("beansEndpointEnabled", beansEndpoint.isEnabled());
        model.put("mappingsEndpointEnabled", requestMappingEndpoint.isEnabled());
        model.put("configPropsEndpointEnabled", configPropertiesEndpoint.isEnabled());
        model.put("dumpEndpointEnabled", dumpEndpoint.isEnabled());
        model.put("infoEndpointEnabled", infoEndpoint.isEnabled());
        model.put("healthEndpointEnabled", healthEndpoint.isEnabled());
        model.put("traceEndpointEnabled", healthEndpoint.isEnabled());

        model.put("trustedDevicesEnabled", this.applicationContext.containsBean("trustedDevicesController")
                && isEndpointCapable(casProperties.getMonitor().getEndpoints().getTrustedDevices(), casProperties));
        model.put("authenticationEventsRepositoryEnabled", this.applicationContext.containsBean("casEventRepository")
                && isEndpointCapable(casProperties.getMonitor().getEndpoints().getAuthenticationEvents(), casProperties));
        model.put("singleSignOnReportEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getSingleSignOnReport(), casProperties));
        model.put("statisticsEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getStatistics(), casProperties));
        model.put("singleSignOnStatusEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getSingleSignOnStatus(), casProperties));
        model.put("springWebflowEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getSpringWebflowReport(), casProperties));
        model.put("auditLogEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getAuditEvents(), casProperties));
        model.put("configurationStateEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getConfigurationState(), casProperties));
        model.put("healthcheckEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getHealthCheck(), casProperties));
        model.put("metricsEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getMetrics(), casProperties));
        model.put("attributeResolutionEndpointEnabled",
                isEndpointCapable(casProperties.getMonitor().getEndpoints().getAttributeResolution(), casProperties));

        final boolean endpointAvailable = model.entrySet().stream()
                .anyMatch(e -> e.getKey().endsWith("Enabled") && BooleanUtils.toBoolean(e.getValue().toString()));
        model.put("dashboardEndpointsEnabled", endpointAvailable);
        model.put("actuatorEndpointsEnabled", casProperties.getAdminPagesSecurity().isActuatorEndpointsEnabled());
        return new ModelAndView("monitoring/viewDashboard", model);
    }
}
