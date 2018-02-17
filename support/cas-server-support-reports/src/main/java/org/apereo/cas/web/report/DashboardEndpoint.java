package org.apereo.cas.web.report;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.monitor.MonitorProperties;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.beans.BeansEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.management.ThreadDumpEndpoint;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.boot.actuate.web.trace.HttpTraceEndpoint;
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
 * This is {@link DashboardEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Endpoint(id = "dashboard")
public class DashboardEndpoint extends BaseCasMvcEndpoint {

    @Autowired(required = false)
    private RestartEndpoint restartEndpoint;

    @Autowired(required = false)
    private ShutdownEndpoint shutdownEndpoint;

    @Autowired
    private InfoEndpoint infoEndpoint;

    @Autowired
    private ConfigurationPropertiesReportEndpoint autoConfigurationReportEndpoint;

    @Autowired
    private BeansEndpoint beansEndpoint;

    @Autowired
    private ThreadDumpEndpoint dumpEndpoint;

    @Autowired
    private ConfigurationPropertiesReportEndpoint configPropertiesEndpoint;

    @Autowired
    private MappingsEndpoint requestMappingEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private HttpTraceEndpoint traceEndpoint;

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    public DashboardEndpoint(final CasConfigurationProperties casProperties) {
        super(casProperties.getMonitor().getEndpoints().getDashboard(), casProperties);
    }

    /**
     * Handle request internal model and view.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    @ReadOperation
    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) {
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
    @ReadOperation
    public Set<EndpointBean> getEndpoints(final HttpServletRequest request, final HttpServletResponse response) {
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
        processSpringBootEndpoints(model);
        processCasProvidedEndpoints(model);
        final boolean endpointAvailable = model.entrySet().stream()
            .anyMatch(e -> e.getKey().endsWith("Enabled") && BooleanUtils.toBoolean(e.getValue().toString()));
        model.put("dashboardEndpointsEnabled", endpointAvailable);
        model.put("actuatorEndpointsEnabled", getCasProperties().getAdminPagesSecurity().isActuatorEndpointsEnabled());
        return model;
    }

    private void processCasProvidedEndpoints(final Map<String, Object> model) {
        final MonitorProperties.Endpoints endpoints = getCasProperties().getMonitor().getEndpoints();
        model.put("trustedDevicesEnabled", this.applicationContext.containsBean("trustedDevicesController"));
        model.put("authenticationEventsRepositoryEnabled", this.applicationContext.containsBean("casEventRepository"));
        model.put("singleSignOnReportEnabled", isEndpointCapable(endpoints.getSingleSignOnReport(), getCasProperties()));
        model.put("statisticsEndpointEnabled", isEndpointCapable(endpoints.getStatistics(), getCasProperties()));
        model.put("singleSignOnStatusEndpointEnabled", isEndpointCapable(endpoints.getSingleSignOnStatus(), getCasProperties()));
        model.put("springWebflowEndpointEnabled", isEndpointCapable(endpoints.getSpringWebflowReport(), getCasProperties()));
        model.put("auditLogEndpointEnabled", isEndpointCapable(endpoints.getAuditEvents(), getCasProperties()));
        model.put("configurationStateEnabled", isEndpointCapable(endpoints.getConfigurationState(), getCasProperties()));
        model.put("healthCheckEndpointEnabled", isEndpointCapable(endpoints.getHealthCheck(), getCasProperties()));
        model.put("metricsEndpointEnabled", isEndpointCapable(endpoints.getMetrics(), getCasProperties()));
        model.put("servicesEndpointEnabled", isEndpointCapable(endpoints.getRegisteredServicesReport(), getCasProperties()));
        model.put("discoveryProfileEndpointEnabled", this.applicationContext.containsBean("casServerProfileRegistrar"))
        model.put("attributeResolutionEndpointEnabled", isEndpointCapable(endpoints.getAttributeResolution(), getCasProperties()));
        model.put("configurationMetadataEndpointEnabled", isEndpointCapable(endpoints.getConfigurationMetadata(), getCasProperties()));
    }

    private void processSpringBootEndpoints(final Map<String, Object> model) {
        model.put("restartEndpointEnabled", isSpringBootEndpointEnabled(restartEndpoint));
        model.put("shutdownEndpointEnabled", isSpringBootEndpointEnabled(shutdownEndpoint));
        model.put("environmentEndpointEnabled", isSpringBootEndpointEnabled(environmentEndpoint));

        model.put("autoConfigurationEndpointEnabled", isSpringBootEndpointEnabled(autoConfigurationReportEndpoint));
        model.put("beansEndpointEnabled", isSpringBootEndpointEnabled(beansEndpoint));
        model.put("mappingsEndpointEnabled", isSpringBootEndpointEnabled(requestMappingEndpoint));
        model.put("configPropsEndpointEnabled", isSpringBootEndpointEnabled(configPropertiesEndpoint));
        model.put("dumpEndpointEnabled", isSpringBootEndpointEnabled(dumpEndpoint));
        model.put("infoEndpointEnabled", isSpringBootEndpointEnabled(infoEndpoint));
        model.put("healthEndpointEnabled", isSpringBootEndpointEnabled(healthEndpoint));
        model.put("traceEndpointEnabled", isSpringBootEndpointEnabled(traceEndpoint));

        model.put("serverFunctionsEnabled", isSpringBootEndpointEnabled(restartEndpoint) || isSpringBootEndpointEnabled(shutdownEndpoint));
    }

    /**
     * The Endpoint bean that holds info about each available endpoint.
     */
    @Data
    public static class EndpointBean implements Serializable {
        private static final long serialVersionUID = -3446962071459197099L;
        private String name;
        private String title;
    }
}
