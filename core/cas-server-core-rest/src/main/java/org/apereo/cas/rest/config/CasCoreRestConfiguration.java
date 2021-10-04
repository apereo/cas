package org.apereo.cas.rest.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.rest.authentication.DefaultRestAuthenticationService;
import org.apereo.cas.rest.authentication.RestAuthenticationService;
import org.apereo.cas.rest.factory.ChainingRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.rest.factory.UsernamePasswordRestHttpRequestCredentialFactory;
import org.apereo.cas.rest.plan.RestHttpRequestCredentialFactoryConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * This is {@link CasCoreRestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Configuration(value = "CasCoreRestConfiguration", proxyBeanMethods = false)
@Slf4j
public class CasCoreRestConfiguration {

    @Configuration(value = "CasCoreRestAuthenticationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreRestAuthenticationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "restAuthenticationService")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestAuthenticationService restAuthenticationService(
            @Qualifier("restHttpRequestCredentialFactory")
            final RestHttpRequestCredentialFactory restHttpRequestCredentialFactory,
            @Qualifier("defaultMultifactorTriggerSelectionStrategy")
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier("webApplicationServiceFactory")
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
            final AuthenticationSystemSupport authenticationSystemSupport,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("requestedContextValidator")
            final RequestedAuthenticationContextValidator requestedContextValidator) {
            return new DefaultRestAuthenticationService(
                authenticationSystemSupport, restHttpRequestCredentialFactory,
                webApplicationServiceFactory, multifactorTriggerSelectionStrategy,
                servicesManager, requestedContextValidator);
        }

    }

    @Configuration(value = "CasCoreRestCredentialFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreRestCredentialFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "restHttpRequestCredentialFactory")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestHttpRequestCredentialFactory restHttpRequestCredentialFactory(
            final List<RestHttpRequestCredentialFactoryConfigurer> configurers) {

            LOGGER.trace("building REST credential factory from [{}]", configurers);
            val factory = new ChainingRestHttpRequestCredentialFactory();
            AnnotationAwareOrderComparator.sortIfNecessary(configurers);

            configurers.forEach(c -> {
                LOGGER.trace("Configuring credential factory: [{}]", c);
                c.configureCredentialFactory(factory);
            });
            return factory;
        }

    }


    @Configuration(value = "CasCoreRestCredentialFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreRestCredentialFactoryPlanConfiguration {

        @ConditionalOnMissingBean(name = "restHttpRequestCredentialFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RestHttpRequestCredentialFactoryConfigurer restHttpRequestCredentialFactoryConfigurer() {
            return factory -> factory.registerCredentialFactory(new UsernamePasswordRestHttpRequestCredentialFactory());
        }
    }
}
