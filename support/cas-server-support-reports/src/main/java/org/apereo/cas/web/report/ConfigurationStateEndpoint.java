package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@Endpoint(id = "configurationState")
public class ConfigurationStateEndpoint extends BaseCasMvcEndpoint {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    @Autowired(required = false)
    private RefreshEndpoint refreshEndpoint;

    @Autowired(required = false)
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired(required = false)
    @Qualifier("configurationPropertiesEnvironmentManager")
    private CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    public ConfigurationStateEndpoint(final CasConfigurationProperties casProperties) {
        super(casProperties.getMonitor().getEndpoints().getConfigurationState(), casProperties);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    @ReadOperation
    public ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {

        final Map<String, Object> model = new HashMap<>();
        final String path = request.getContextPath();
        ControllerUtils.configureModelMapForConfigServerCloudBusEndpoints(path, model);
        model.put("enableRefresh", isRefreshEnabled());
        model.put("enableUpdate", isUpdateEnabled());
        return new ModelAndView(VIEW_CONFIG, model);
    }

    private Boolean isRefreshEnabled() {
        return !getCasProperties().getEvents().isTrackConfigurationModifications()
            && refreshEndpoint != null
            && isUpdateEnabled();
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
    @ReadOperation
    public Map getConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
        final Map results = new TreeMap();
        if (environmentEndpoint == null) {
            LOGGER.warn("Environment endpoint is either undefined or disabled");
            return results;
        }

        final Pattern pattern = RegexUtils.createPattern("(configService:|applicationConfig:).+(application|cas).+");
        final Map<String, Object> environmentSettings = environmentEndpoint.environment(".+");
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

    /**
     * Update configuration map.
     *
     * @param jsonInput the json input
     * @param request   the request
     * @param response  the response
     */
    @PostMapping("/updateConfiguration")
    @ResponseBody
    @WriteOperation
    public void updateConfiguration(@RequestBody final Map<String, Map<String, String>> jsonInput,
                                    final HttpServletRequest request,
                                    final HttpServletResponse response) {

        if (isUpdateEnabled()) {
            final Map<String, String> newData = jsonInput.get("new");
            configurationPropertiesEnvironmentManager.savePropertyForStandaloneProfile(Pair.of(newData.get("key"), newData.get("value")));
            eventPublisher.publishEvent(new CasConfigurationModifiedEvent(this, !getCasProperties().getEvents().isTrackConfigurationModifications()));
        }
    }
}
