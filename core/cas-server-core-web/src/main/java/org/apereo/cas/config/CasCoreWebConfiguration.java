package org.apereo.cas.config;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.RootCasException;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.UnknownTenantException;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.web.support.MappedExceptionErrorViewResolver;
import org.apereo.cas.util.spring.RestActuatorControllerEndpoint;
import org.apereo.cas.util.spring.RestActuatorEndpointDiscoverer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.CasYamlHttpMessageConverter;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.apereo.cas.web.view.DynamicHtmlView;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.webflow.conversation.NoSuchConversationException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreWebAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@Configuration(value = "CasCoreWebConfiguration", proxyBeanMethods = false)
class CasCoreWebConfiguration {

    @Configuration(value = "CasCoreWebMessageSourceConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebMessageSourceConfiguration {
        /**
         * Load property files containing non-i18n fallback values
         * that should be exposed to Thyme templates.
         * keys in properties files added last will take precedence over the
         * internal cas_common_messages.properties.
         * Keys in regular messages bundles will override any of the common messages.
         *
         * @param casProperties the cas properties
         * @return PropertiesFactoryBean containing all common (non-i18n) messages
         */
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casCommonMessages")
        public PropertiesFactoryBean casCommonMessages(final CasConfigurationProperties casProperties) {
            val properties = new PropertiesFactoryBean();
            val resourceLoader = new DefaultResourceLoader();
            val commonNames = casProperties.getMessageBundle().getCommonNames();

            val resourceList = commonNames
                .stream()
                .map(resourceLoader::getResource)
                .collect(Collectors.toList());
            resourceList.add(resourceLoader.getResource("classpath:/cas_common_messages.properties"));
            properties.setLocations(resourceList.toArray(Resource[]::new));
            properties.setSingleton(true);
            properties.setIgnoreResourceNotFound(true);
            return properties;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "casMessageSource")
        public MessageSource messageSource(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final CasConfigurationProperties casProperties,
            @Qualifier("casCommonMessages")
            final Properties casCommonMessages) {
            val messageBundle = new CasReloadableMessageBundle(tenantExtractor);
            messageBundle.setCommonMessages(casCommonMessages);
            return CasReloadableMessageBundle.configure(messageBundle, casProperties);
        }
    }

    @Configuration(value = "CasCoreWebRequestsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebRequestsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = ArgumentExtractor.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ArgumentExtractor argumentExtractor(final List<ServiceFactoryConfigurer> configurers) {
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);
            val serviceFactoryList = configurers.stream()
                .flatMap(configurer -> configurer.buildServiceFactories().stream())
                .collect(Collectors.toList());
            return new DefaultArgumentExtractor(serviceFactoryList);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = UrlValidator.BEAN_NAME)
        public FactoryBean<UrlValidator> urlValidator(final CasConfigurationProperties casProperties) {
            val httpClient = casProperties.getHttpClient();
            val allowLocalLogoutUrls = httpClient.isAllowLocalUrls();
            val authorityValidationRegEx = httpClient.getAuthorityValidationRegex();
            val authorityValidationRegExCaseSensitive = httpClient.isAuthorityValidationRegExCaseSensitive();
            return new SimpleUrlValidatorFactoryBean(allowLocalLogoutUrls, authorityValidationRegEx,
                authorityValidationRegExCaseSensitive);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "yamlHttpMessageConverter")
        public HttpMessageConverter yamlHttpMessageConverter() {
            return new CasYamlHttpMessageConverter();
        }

        @Bean
        @ConditionalOnMissingBean(name = "defaultMappedExceptionErrorViewResolver")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ErrorViewResolver defaultMappedExceptionErrorViewResolver(
            final WebProperties webProperties,
            final ConfigurableApplicationContext applicationContext) {
            val mappings = Map.<Class<? extends Throwable>, ModelAndView>of(
                UnknownTenantException.class, WebUtils.toModelAndView(HttpStatus.NOT_FOUND, CasWebflowConstants.VIEW_ID_UNKNOWN_TENANT),
                UnauthorizedServiceException.class, WebUtils.toModelAndView(HttpStatus.FORBIDDEN, CasWebflowConstants.VIEW_ID_SERVICE_ERROR),
                NoSuchConversationException.class, WebUtils.toModelAndView(HttpStatus.UNPROCESSABLE_ENTITY, "error/%s".formatted(HttpStatus.UNPROCESSABLE_ENTITY.value())),
                RootCasException.class, WebUtils.toModelAndView(HttpStatus.BAD_REQUEST, CasWebflowConstants.VIEW_ID_SERVICE_ERROR)
            );
            return new MappedExceptionErrorViewResolver(applicationContext,
                webProperties.getResources(), mappings, errorContext -> Optional.empty());
        }
    }

    @Configuration(value = "CasCoreWebViewsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreWebViewsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.VIEW_ID_DYNAMIC_HTML)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public View dynamicHtmlView() {
            return (model, request, response) -> {
                val html = (String) Objects.requireNonNull(model).get(DynamicHtmlView.class.getName());
                new DynamicHtmlView(html).render(model, request, response);
            };
        }
    }

    @Configuration(value = "CasCoreWebEndpointsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties({CasConfigurationProperties.class, CorsEndpointProperties.class, WebProperties.class})
    static class CasCoreWebEndpointsConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "casProtocolEndpointConfigurer")
        public CasWebSecurityConfigurer<Void> casProtocolEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(
                        StringUtils.prependIfMissing(CasProtocolConstants.ENDPOINT_LOGIN, "/"),
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
    }

    @Configuration(value = "CasActuatorEndpointsConfiguration", proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnManagementPort(ManagementPortType.SAME)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Import(CasCoreActuatorsConfiguration.class)
    static class CasActuatorEndpointsConfiguration {
    }

    /**
     * This configuration class registers an {@link EndpointsSupplier}
     * in the root/parent application context, so that endpoints discovered
     * here can be used by the {@link org.springframework.boot.actuate.endpoint.web.PathMappedEndpoint}
     * when the security filter chain is configured.
     * This is only required if actuator endpoints are defined to run in a separate/child
     * application (management) context.
     * @see CasCoreWebManagementContextConfiguration
     */
    @Configuration(value = "CasRestActuatorEndpointsConfiguration", proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnManagementPort(ManagementPortType.DIFFERENT)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasRestActuatorEndpointsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "restControllerEndpointSupplier")
        public EndpointsSupplier<RestActuatorControllerEndpoint> restControllerEndpointSupplier(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<PathMapper> endpointPathMappers,
            final ObjectProvider<Collection<EndpointFilter<RestActuatorControllerEndpoint>>> filters) {
            return new RestActuatorEndpointDiscoverer(applicationContext,
                endpointPathMappers.orderedStream().toList(),
                filters.getIfAvailable(Collections::emptyList));
        }
    }
}
