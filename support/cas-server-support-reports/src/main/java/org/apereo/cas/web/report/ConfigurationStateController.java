package org.apereo.cas.web.report;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at {@code /status/config}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ConfigurationStateController extends BaseCasMvcEndpoint {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    @Autowired
    private RefreshEndpoint refreshEndpoint;

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired(required = false)
    @Qualifier("configurationPropertiesEnvironmentManager")
    private CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    private final CasConfigurationProperties casProperties;

    public ConfigurationStateController(final CasConfigurationProperties casProperties) {
        super("configstate", "/config", casProperties.getMonitor().getEndpoints().getConfigurationState(), casProperties);
        this.casProperties = casProperties;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Map<String, Object> model = new HashMap<>();
        final String path = request.getContextPath();
        ControllerUtils.configureModelMapForConfigServerCloudBusEndpoints(path, model);
        model.put("enableRefresh", isRefreshEnabled());
        model.put("enableUpdate", isUpdateEnabled());
        return new ModelAndView(VIEW_CONFIG, model);
    }

    private Boolean isRefreshEnabled() {
        return !casProperties.getEvents().isTrackConfigurationModifications() && refreshEndpoint.isEnabled()
                && environment.getProperty("spring.cloud.config.enabled", Boolean.class);
    }

    private Boolean isUpdateEnabled() {
        return !environment.getProperty("spring.cloud.config.enabled", Boolean.class);
    }

    /**
     * Gets configuration.
     *
     * @param request  the request
     * @param response the response
     * @return the configuration
     */
    @GetMapping("/getConfiguration")
    @ResponseBody
    public Map getConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        final Pattern pattern = RegexUtils.createPattern("(configService:|applicationConfig:).+(application|cas).+");

        if (environmentEndpoint.isEnabled()) {
            final Map results = new TreeMap();
            final Map<String, Object> environmentSettings = environmentEndpoint.invoke();
            environmentSettings.entrySet()
                    .stream()
                    .filter(entry -> pattern.matcher(entry.getKey()).matches())
                    .forEach(entry -> {
                        final Map<String, Object> keys = (Map<String, Object>) entry.getValue();
                        keys.keySet().forEach(key -> {
                            if (!results.containsKey(key)) {
                                final String propHolder = String.format("${%s}", key);
                                final String value = this.environment.resolvePlaceholders(propHolder);
                                results.put(key, environmentEndpoint.sanitize(key, value));
                            }
                        });
                    });

            return results;
        }

        return new LinkedHashMap();
    }

    /**
     * Update configuration map.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     */
    @PostMapping("/updateConfiguration")
    @ResponseBody
    public void updateConfiguration(@RequestBody final Map<String, Map<String, String>> jsonInput,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        if (isUpdateEnabled()) {
            final Map<String, String> newData = jsonInput.get("new");
            configurationPropertiesEnvironmentManager.savePropertyForStandaloneProfile(Pair.of(newData.get("key"), newData.get("value")));
            eventPublisher.publishEvent(new CasConfigurationModifiedEvent(this, !casProperties.getEvents().isTrackConfigurationModifications()));
        }
    }
}
