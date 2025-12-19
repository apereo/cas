package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.util.spring.RestActuatorControllerEndpoint;
import org.apereo.cas.util.spring.RestActuatorEndpointDiscoverer;
import org.apereo.cas.util.spring.RestActuatorEndpointHandlerMapping;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.webmvc.actuate.endpoint.web.WebMvcEndpointHandlerMapping;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * This is {@link CasCoreActuatorsConfiguration}.
 * Beans in this class are conditionally imported
 * depending on whether management endpoints are to be deployed
 * in a separate management context. 
 * @author Misagh Moayyed
 * @see CasCoreWebManagementContextConfiguration
 * @since 7.2.0
 */
@Configuration(value = "CasCoreActuatorsConfiguration", proxyBeanMethods = false)
@Lazy(false)
@SuppressWarnings("AutoConfigurationRequired")
class CasCoreActuatorsConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "restControllerEndpointDiscoverer")
    public RestActuatorEndpointDiscoverer restControllerEndpointDiscoverer(
        final ConfigurableApplicationContext applicationContext,
        final ObjectProvider<@NonNull PathMapper> endpointPathMappers,
        final ObjectProvider<@NonNull Collection<EndpointFilter<@NonNull RestActuatorControllerEndpoint>>> filters) {
        return new RestActuatorEndpointDiscoverer(applicationContext,
            endpointPathMappers.orderedStream().toList(),
            filters.getIfAvailable(Collections::emptyList));
    }

    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
        final EndpointLinksResolver endpointLinksResolver,
        final WebEndpointsSupplier webEndpointsSupplier,
        @Qualifier("restControllerEndpointDiscoverer")
        final EndpointsSupplier<@NonNull RestActuatorControllerEndpoint> restEndpointsSupplier,
        final EndpointMediaTypes endpointMediaTypes,
        final CorsEndpointProperties corsProperties,
        final WebEndpointProperties webEndpointProperties,
        final Environment environment) {
        val webEndpoints = webEndpointsSupplier.getEndpoints();
        val basePath = webEndpointProperties.getBasePath();
        val endpointMapping = new EndpointMapping(basePath);
        val shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
            corsProperties.toCorsConfiguration(), endpointLinksResolver, shouldRegisterLinksMapping);
    }

    @Bean
    public EndpointLinksResolver endpointLinksResolver(
        final WebEndpointsSupplier webEndpointsSupplier,
        @Qualifier("restControllerEndpointDiscoverer")
        final EndpointsSupplier<@NonNull RestActuatorControllerEndpoint> restEndpointsSupplier,
        final EndpointMediaTypes endpointMediaTypes,
        final CorsEndpointProperties corsProperties,
        final WebEndpointProperties webEndpointProperties) {
        val basePath = webEndpointProperties.getBasePath();
        val allEndpoints = new ArrayList<ExposableEndpoint<?>>();
        val webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(restEndpointsSupplier.getEndpoints());
        return new EndpointLinksResolver(allEndpoints, basePath);
    }

    @Bean
    @ConditionalOnMissingBean(name = "restControllerEndpointHandlerMapping")
    public RestActuatorEndpointHandlerMapping restControllerEndpointHandlerMapping(
        @Qualifier("restControllerEndpointDiscoverer")
        final EndpointsSupplier<@NonNull RestActuatorControllerEndpoint> restEndpointsSupplier,
        final CorsEndpointProperties corsProperties, final WebEndpointProperties webEndpointProperties) {
        val endpointMapping = new EndpointMapping(webEndpointProperties.getBasePath());
        return new RestActuatorEndpointHandlerMapping(endpointMapping, restEndpointsSupplier.getEndpoints(),
            corsProperties.toCorsConfiguration());
    }
    
    private static boolean shouldRegisterLinksMapping(
        final WebEndpointProperties webEndpointProperties,
        final Environment environment,
        final String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled()
            && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment) == ManagementPortType.DIFFERENT);
    }
}
