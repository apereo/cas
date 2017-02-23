package org.apereo.cas.web.report;

import org.apache.commons.collections.map.LinkedMap;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.report.util.ControllerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.EnvironmentEndpoint;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.config.server.config.ConfigServerProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * Controller that exposes the CAS internal state and beans
 * as JSON. The report is available at {@code /status/config}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class ConfigurationStateController extends BaseCasMvcEndpoint {

    private static final String VIEW_CONFIG = "monitoring/viewConfig";

    @Autowired(required = false)
    private BusProperties busProperties;

    @Autowired
    private ConfigServerProperties configServerProperties;

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    public ConfigurationStateController(final CasConfigurationProperties casProperties) {
        super("configstate", "/config", casProperties.getMonitor().getEndpoints().getConfigurationState());
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        this.environment.getPropertySources().addFirst(new CasOverridingPropertySource());
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
        ensureEndpointAccessIsAuthorized(request, response);
        
        final Map<String, Object> model = new HashMap<>();
        final String path = request.getContextPath();
        ControllerUtils.configureModelMapForConfigServerCloudBusEndpoints(busProperties, configServerProperties, path, model);
        return new ModelAndView(VIEW_CONFIG, model);
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
    protected Map getConfiguration(final HttpServletRequest request, final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);
        
        final String patternStr = String.format("(%s|configService:|applicationConfig:).+(application|cas).+", CasOverridingPropertySource.SOURCE_NAME);
        final Pattern pattern = RegexUtils.createPattern(patternStr);

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
    protected void updateConfiguration(@RequestBody final Map<String, Map<String, String>> jsonInput,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response) {

        ensureEndpointAccessIsAuthorized(request, response);
        
        final Map<String, String> oldData = jsonInput.get("old");
        final Map<String, String> newData = jsonInput.get("new");

        final Optional<PropertySource<?>> src = StreamSupport.stream(environment.getPropertySources().spliterator(), true)
                .filter(CasOverridingPropertySource.class::isInstance)
                .findAny();

        final CasOverridingPropertySource source;
        if (src.isPresent()) {
            source = (CasOverridingPropertySource) src.get();
            source.setProperty(newData.get("key"), newData.get("value"));
            eventPublisher.publishEvent(new CasConfigurationModifiedEvent(source, true));
        }
    }

    /**
     * The type Cas overriding property source.
     */
    public static class CasOverridingPropertySource extends PropertiesPropertySource {
        /**
         * Property source name.
         */
        public static final String SOURCE_NAME = "casConfigPanel";

        protected CasOverridingPropertySource() {
            super(SOURCE_NAME, new LinkedMap());
        }

        /**
         * Sets property.
         *
         * @param key   the key
         * @param value the value
         */
        public void setProperty(final String key, final String value) {
            getSource().remove(key);
            getSource().put(key, value);
        }
    }
}
