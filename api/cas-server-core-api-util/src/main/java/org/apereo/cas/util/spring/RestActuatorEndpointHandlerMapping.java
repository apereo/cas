package org.apereo.cas.util.spring;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import jakarta.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RestActuatorEndpointHandlerMapping}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class RestActuatorEndpointHandlerMapping extends RequestMappingHandlerMapping {

    private final EndpointMapping endpointMapping;

    private final CorsConfiguration corsConfiguration;

    private final Map<Object, RestActuatorControllerEndpoint> handlers;

    public RestActuatorEndpointHandlerMapping(final EndpointMapping endpointMapping,
                                              final Collection<RestActuatorControllerEndpoint> endpoints,
                                              final CorsConfiguration corsConfiguration) {
        this.endpointMapping = endpointMapping;
        this.handlers = getHandlers(endpoints);
        this.corsConfiguration = corsConfiguration;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    private static Map<Object, RestActuatorControllerEndpoint> getHandlers(final Collection<RestActuatorControllerEndpoint> endpoints) {
        val handlers = new LinkedHashMap<Object, RestActuatorControllerEndpoint>();
        endpoints.forEach(endpoint -> handlers.put(endpoint.getEndpointBean(), endpoint));
        return Map.copyOf(handlers);
    }

    @Override
    protected void initHandlerMethods() {
        this.handlers.keySet().forEach(this::detectHandlerMethods);
    }

    @Override
    protected void registerHandlerMethod(@Nonnull final Object handler, @Nonnull final Method method, @Nonnull final RequestMappingInfo mapping) {
        val endpoint = this.handlers.get(handler);
        val mappingWithPatterns = withEndpointMappedPatterns(endpoint, mapping);
        super.registerHandlerMethod(handler, method, mappingWithPatterns);
    }

    private RequestMappingInfo withEndpointMappedPatterns(final RestActuatorControllerEndpoint endpoint,
                                                          final RequestMappingInfo mapping) {
        var patterns = mapping.getPathPatternsCondition().getPatterns();
        if (patterns.isEmpty()) {
            patterns = Set.of(getPatternParser().parse(StringUtils.EMPTY));
        }
        val endpointMappedPatterns = patterns.stream()
            .map(pattern -> getEndpointMappedPattern(endpoint, pattern))
            .toArray(String[]::new);
        return mapping.mutate().paths(endpointMappedPatterns).build();
    }

    private String getEndpointMappedPattern(final RestActuatorControllerEndpoint endpoint, final PathPattern pattern) {
        return this.endpointMapping.createSubPath(endpoint.getRootPath() + pattern);
    }

    @Override
    protected boolean hasCorsConfigurationSource(@Nonnull final Object handler) {
        return this.corsConfiguration != null;
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(@Nonnull final Object handler,
                                                      @Nonnull final Method method,
                                                      @Nonnull final RequestMappingInfo mapping) {
        return this.corsConfiguration;
    }
}
