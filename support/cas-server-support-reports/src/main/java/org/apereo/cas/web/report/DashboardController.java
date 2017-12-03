package org.apereo.cas.web.report;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DashboardController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DashboardController extends BaseCasMvcEndpoint {

    @Autowired(required = false)
    private RestartEndpoint restartEndpoint;

    @Autowired(required = false)
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

    public DashboardController(final CasConfigurationProperties casProperties) {
        super("casdashboard", "/dashboard", casProperties.getMonitor().getEndpoints().getDashboard(), casProperties);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        final Map<String, Object> model = getEndpointsModelMap();
        return new ModelAndView("monitoring/viewDashboard", model);
    }

    /**
     * Gets endpoints.
     *
     * @param request  the request
     * @param response the response
     * @return the endpoints
     */
    @GetMapping(value = "/endpoints")
    @ResponseBody
    public Set<EndpointBean> getEndpoints(final HttpServletRequest request,
                                          final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Map<String, Object> endpointsModel = getEndpointsModelMap();
        return endpointsModel.entrySet()
            .stream()
            .map(entry -> {
                final EndpointBean bean = new EndpointBean();
                bean.setName(StringUtils.remove(entry.getKey(), "Enabled"));
                String title = StringUtils.capitalize(StringUtils.remove(bean.getName(), "Endpoint"));
                title = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(title), ' ');
                bean.setTitle(title);
                return bean;
            })
            .collect(Collectors.toSet());
    }

    private Map<String, Object> getEndpointsModelMap() {
        final Map<String, Object> model = new HashMap<>();

        model.put("restartEndpointEnabled", isNativeEndpointEnabled(restartEndpoint));
        model.put("shutdownEndpointEnabled", isNativeEndpointEnabled(shutdownEndpoint));
        model.put("environmentEndpointEnabled", isNativeEndpointEnabled(environmentEndpoint));
        model.put("serverFunctionsEnabled", isNativeEndpointEnabled(restartEndpoint) || isNativeEndpointEnabled(shutdownEndpoint));
        model.put("autoConfigurationEndpointEnabled", isNativeEndpointEnabled(autoConfigurationReportEndpoint));
        model.put("beansEndpointEnabled", isNativeEndpointEnabled(beansEndpoint));
        model.put("mappingsEndpointEnabled", isNativeEndpointEnabled(requestMappingEndpoint));
        model.put("configPropsEndpointEnabled", isNativeEndpointEnabled(configPropertiesEndpoint));
        model.put("dumpEndpointEnabled", isNativeEndpointEnabled(dumpEndpoint));
        model.put("infoEndpointEnabled", isNativeEndpointEnabled(infoEndpoint));
        model.put("healthEndpointEnabled", isNativeEndpointEnabled(healthEndpoint));
        model.put("traceEndpointEnabled", isNativeEndpointEnabled(traceEndpoint));

        final MonitorProperties.Endpoints endpoints = casProperties.getMonitor().getEndpoints();
        model.put("trustedDevicesEnabled", this.applicationContext.containsBean("trustedDevicesController")
            && isEndpointCapable(endpoints.getTrustedDevices(), casProperties));
        model.put("authenticationEventsRepositoryEnabled", this.applicationContext.containsBean("casEventRepository")
            && isEndpointCapable(endpoints.getAuthenticationEvents(), casProperties));
        model.put("singleSignOnReportEnabled", isEndpointCapable(endpoints.getSingleSignOnReport(), casProperties));
        model.put("statisticsEndpointEnabled", isEndpointCapable(endpoints.getStatistics(), casProperties));
        model.put("singleSignOnStatusEndpointEnabled", isEndpointCapable(endpoints.getSingleSignOnStatus(), casProperties));
        model.put("springWebflowEndpointEnabled", isEndpointCapable(endpoints.getSpringWebflowReport(), casProperties));
        model.put("auditLogEndpointEnabled", isEndpointCapable(endpoints.getAuditEvents(), casProperties));
        model.put("configurationStateEnabled", isEndpointCapable(endpoints.getConfigurationState(), casProperties));
        model.put("healthcheckEndpointEnabled", isEndpointCapable(endpoints.getHealthCheck(), casProperties));
        model.put("metricsEndpointEnabled", isEndpointCapable(endpoints.getMetrics(), casProperties));
        model.put("servicesEndpointEnabled", isEndpointCapable(endpoints.getRegisteredServicesReport(), casProperties));
        model.put("discoveryProfileEndpointEnabled", this.applicationContext.containsBean("casServerProfileRegistrar")
            && isEndpointCapable(endpoints.getDiscovery(), casProperties));
        model.put("attributeResolutionEndpointEnabled", isEndpointCapable(endpoints.getAttributeResolution(), casProperties));
        model.put("configurationMetadataEndpointEnabled", isEndpointCapable(endpoints.getConfigurationMetadata(), casProperties));

        final boolean endpointAvailable = model.entrySet().stream()
            .anyMatch(e -> e.getKey().endsWith("Enabled") && BooleanUtils.toBoolean(e.getValue().toString()));
        model.put("dashboardEndpointsEnabled", endpointAvailable);
        model.put("actuatorEndpointsEnabled", casProperties.getAdminPagesSecurity().isActuatorEndpointsEnabled());
        return model;
    }

    private boolean isNativeEndpointEnabled(final AbstractEndpoint endpoint) {
        return endpoint != null && endpoint.isEnabled();
    }

    /**
     * The Endpoint bean that holds info about each available endpoint.
     */
    public static class EndpointBean implements Serializable {
        private static final long serialVersionUID = -3446962071459197099L;
        private String name;
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
