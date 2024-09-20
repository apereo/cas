package org.apereo.cas.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.RestActuatorControllerEndpoint;
import org.apereo.cas.util.spring.RestActuatorEndpointDiscoverer;
import org.apereo.cas.util.spring.RestActuatorEndpointHandlerMapping;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;

/**
 * This is {@link CasCoreWebEndpointsConfiguration}.
 *
 * @author Janis Semper
 * @since 7.2.0
 */
@Configuration(value = "CasCoreWebEndpointsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties({ CasConfigurationProperties.class, CorsEndpointProperties.class, WebProperties.class })
public class CasCoreWebEndpointsConfiguration {
	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = "casProtocolEndpointConfigurer")
	public CasWebSecurityConfigurer<Void> casProtocolEndpointConfigurer() {
		return new CasWebSecurityConfigurer<>() {
			@Override
			public List<String> getIgnoredEndpoints() {
				return List.of(StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_LOGIN, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_LOGOUT, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_VALIDATE, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_SERVICE_VALIDATE_V3, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_PROXY_VALIDATE, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_PROXY_VALIDATE_V3, "/"),
						StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_PROXY, "/"));
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(name = "restControllerEndpointDiscoverer")
	public RestActuatorEndpointDiscoverer restControllerEndpointDiscoverer(
			final ConfigurableApplicationContext applicationContext,
			final ObjectProvider<PathMapper> endpointPathMappers,
			final ObjectProvider<Collection<EndpointFilter<RestActuatorControllerEndpoint>>> filters) {
		return new RestActuatorEndpointDiscoverer(applicationContext, endpointPathMappers.orderedStream().toList(),
				filters.getIfAvailable(Collections::emptyList));
	}

	@Bean
	public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
			final EndpointLinksResolver endpointLinksResolver, final WebEndpointsSupplier webEndpointsSupplier,
			final EndpointsSupplier<RestActuatorControllerEndpoint> restEndpointsSupplier,
			final EndpointMediaTypes endpointMediaTypes, final CorsEndpointProperties corsProperties,
			final WebEndpointProperties webEndpointProperties, final Environment environment) {
		val webEndpoints = webEndpointsSupplier.getEndpoints();
		val basePath = webEndpointProperties.getBasePath();
		val endpointMapping = new EndpointMapping(basePath);
		val shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
		return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
				corsProperties.toCorsConfiguration(), endpointLinksResolver, shouldRegisterLinksMapping);
	}

	@Bean
	public EndpointLinksResolver endpointLinksResolver(final WebEndpointsSupplier webEndpointsSupplier,
			final EndpointsSupplier<RestActuatorControllerEndpoint> restEndpointsSupplier,
			final EndpointMediaTypes endpointMediaTypes, final CorsEndpointProperties corsProperties,
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
			final EndpointsSupplier<RestActuatorControllerEndpoint> restEndpointsSupplier,
			final CorsEndpointProperties corsProperties, final WebEndpointProperties webEndpointProperties) {
		val endpointMapping = new EndpointMapping(webEndpointProperties.getBasePath());
		return new RestActuatorEndpointHandlerMapping(endpointMapping, restEndpointsSupplier.getEndpoints(),
				corsProperties.toCorsConfiguration());
	}

	private static boolean shouldRegisterLinksMapping(final WebEndpointProperties webEndpointProperties,
			final Environment environment, final String basePath) {
		return webEndpointProperties.getDiscovery().isEnabled()
				&& (org.springframework.util.StringUtils.hasText(basePath)
						|| ManagementPortType.get(environment) == ManagementPortType.DIFFERENT);
	}
}
