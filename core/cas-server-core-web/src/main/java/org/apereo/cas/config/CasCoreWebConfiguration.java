package org.apereo.cas.config;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.web.support.MappedExceptionErrorViewResolver;
import org.apereo.cas.util.spring.RestActuatorEndpoint;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.CasYamlHttpMessageConverter;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.CasReloadableMessageBundle;
import org.apereo.cas.web.view.DynamicHtmlView;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.actuate.endpoint.EndpointFilter;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.Operation;
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper;
import org.springframework.boot.actuate.endpoint.web.PathMapper;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ExposableControllerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.webflow.conversation.NoSuchConversationException;
import java.util.ArrayList;
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
        public HierarchicalMessageSource messageSource(
            final CasConfigurationProperties casProperties,
            @Qualifier("casCommonMessages") final Properties casCommonMessages) {
            val bean = new CasReloadableMessageBundle();
            val mb = casProperties.getMessageBundle();
            bean.setDefaultEncoding(mb.getEncoding());
            bean.setCacheSeconds(Long.valueOf(Beans.newDuration(mb.getCacheSeconds()).toSeconds()).intValue());
            bean.setFallbackToSystemLocale(mb.isFallbackSystemLocale());
            bean.setUseCodeAsDefaultMessage(mb.isUseCodeMessage());
            bean.setBasenames(mb.getBaseNames().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
            bean.setCommonMessages(casCommonMessages);
            return bean;
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
                .collect(Collectors.toCollection(ArrayList::new));
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
                UnauthorizedServiceException.class, getModelAndView(HttpStatus.FORBIDDEN, CasWebflowConstants.VIEW_ID_SERVICE_ERROR),
                NoSuchConversationException.class, getModelAndView(HttpStatus.UNPROCESSABLE_ENTITY, "error/%s".formatted(HttpStatus.UNPROCESSABLE_ENTITY.value()))
            );
            return new MappedExceptionErrorViewResolver(applicationContext,
                webProperties.getResources(), mappings, errorContext -> Optional.empty());
        }

        private static ModelAndView getModelAndView(final HttpStatus status, final String viewName) {
            val mv = new ModelAndView();
            mv.setStatus(HttpStatusCode.valueOf(status.value()));
            mv.setViewName(viewName);
            return mv;
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
    @EnableConfigurationProperties(CasConfigurationProperties.class)
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

        @Bean
        public RestActuatorEndpointDiscoverer controllerEndpointDiscoverer(
            final ConfigurableApplicationContext applicationContext,
            final ObjectProvider<PathMapper> endpointPathMappers,
            final ObjectProvider<Collection<EndpointFilter<ExposableControllerEndpoint>>> filters) {
            return new RestActuatorEndpointDiscoverer(applicationContext,
                endpointPathMappers.orderedStream().toList(),
                filters.getIfAvailable(Collections::emptyList));
        }

        public static class RestActuatorEndpointDiscoverer extends EndpointDiscoverer<ExposableControllerEndpoint, Operation> implements ControllerEndpointsSupplier {
            private final List<PathMapper> endpointPathMappers;

            RestActuatorEndpointDiscoverer(final ApplicationContext applicationContext, final List<PathMapper> endpointPathMappers,
                                           final Collection<EndpointFilter<ExposableControllerEndpoint>> filters) {
                super(applicationContext, ParameterValueMapper.NONE, List.of(), filters);
                this.endpointPathMappers = endpointPathMappers;
            }
            @Override
            protected boolean isEndpointTypeExposed(final Class<?> beanType) {
                val annotations = MergedAnnotations.from(beanType, MergedAnnotations.SearchStrategy.SUPERCLASS);
                return annotations.isPresent(RestActuatorEndpoint.class) && annotations.isPresent(Endpoint.class);
            }
            
            @Override
            protected ExposableControllerEndpoint createEndpoint(final Object endpointBean,
                                                                 final EndpointId id,
                                                                 final boolean enabledByDefault,
                                                                 final Collection<Operation> operations) {
                val rootPath = PathMapper.getRootPath(this.endpointPathMappers, id);
                return new DiscoveredRestActuatorEndpoint(this, endpointBean, id, rootPath, enabledByDefault);
            }
            @Override
            protected Operation createOperation(final EndpointId endpointId, final DiscoveredOperationMethod operationMethod, final OperationInvoker invoker) {
                throw new IllegalStateException("RestActuatorEndpoint must not declare operations");
            }

            @Override
            protected EndpointDiscoverer.OperationKey createOperationKey(final Operation operation) {
                throw new IllegalStateException("RestActuatorEndpoint must not declare operations");
            }
        }

        @Getter
        private static class DiscoveredRestActuatorEndpoint extends AbstractDiscoveredEndpoint<Operation> implements ExposableControllerEndpoint {
            private final String rootPath;

            DiscoveredRestActuatorEndpoint(final EndpointDiscoverer<?, ?> discoverer, final Object endpointBean,
                                           final EndpointId id, final String rootPath, final boolean enabledByDefault) {
                super(discoverer, endpointBean, id, enabledByDefault, List.of());
                this.rootPath = rootPath;
            }
            @Override
            public Object getController() {
                return this.getEndpointBean();
            }
        }

    }
}
