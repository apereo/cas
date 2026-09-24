package org.apereo.cas.util.spring;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.actuate.endpoint.EndpointAccessResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

/**
 * This is {@link RestActuatorEndpointHandlerMapping}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class RestActuatorEndpointHandlerMapping extends RequestMappingHandlerMapping {

    private static final Set<RequestMethod> READ_ONLY_REQUEST_METHODS =
        Set.of(RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS);

    private final EndpointMapping endpointMapping;

    private final CorsConfiguration corsConfiguration;

    private final Map<Object, RestActuatorControllerEndpoint> handlers;

    private final EndpointAccessResolver endpointAccessResolver;

    public RestActuatorEndpointHandlerMapping(final EndpointMapping endpointMapping,
                                              final Collection<RestActuatorControllerEndpoint> endpoints,
                                              final CorsConfiguration corsConfiguration,
                                              final EndpointAccessResolver endpointAccessResolver) {
        this.endpointMapping = endpointMapping;
        this.handlers = getHandlers(endpoints);
        this.corsConfiguration = corsConfiguration;
        this.endpointAccessResolver = endpointAccessResolver;
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
    protected void registerHandlerMethod(@NonNull final Object handler, @NonNull final Method method, @NonNull final RequestMappingInfo mapping) {
        val endpoint = this.handlers.get(handler);
        if (isMappingPermittedByAccessLevel(endpoint, mapping)) {
            val mappingWithPatterns = withEndpointMappedPatterns(endpoint, mapping);
            super.registerHandlerMethod(handler, method, mappingWithPatterns);
        }
    }

    /**
     * Decide whether a mapping may be registered for the access level the endpoint resolves to.
     * <p>
     * Spring Boot enforces an access level by filtering an endpoint's operations, which these
     * endpoints do not have; their handlers are Spring MVC mappings instead, so the same rules are
     * applied to the mappings here. {@code NONE} registers nothing, and {@code READ_ONLY} registers
     * only the mappings restricted to read methods; a mapping that names no method at all accepts
     * writes too, and is therefore not registered under {@code READ_ONLY} either.
     *
     * @param endpoint the endpoint that owns this mapping
     * @param mapping  the mapping
     * @return true if the mapping may be registered
     */
    protected boolean isMappingPermittedByAccessLevel(final RestActuatorControllerEndpoint endpoint,
                                                      final RequestMappingInfo mapping) {
        val access = endpointAccessResolver.accessFor(endpoint.getEndpointId(), endpoint.getDefaultAccess());
        return switch (access) {
            case NONE -> false;
            case READ_ONLY -> {
                val requestMethods = mapping.getMethodsCondition().getMethods();
                yield !requestMethods.isEmpty() && READ_ONLY_REQUEST_METHODS.containsAll(requestMethods);
            }
            case UNRESTRICTED -> true;
        };
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
    protected boolean hasCorsConfigurationSource(@NonNull final Object handler) {
        return this.corsConfiguration != null;
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(@NonNull final Object handler,
                                                      @NonNull final Method method,
                                                      @NonNull final RequestMappingInfo mapping) {
        return this.corsConfiguration;
    }
}
