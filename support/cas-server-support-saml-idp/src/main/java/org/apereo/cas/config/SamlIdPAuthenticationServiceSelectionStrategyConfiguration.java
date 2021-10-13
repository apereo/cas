package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.authentication.SamlIdPServiceFactory;
import org.apereo.cas.support.saml.services.SamlIdPEntityIdAuthenticationServiceSelectionStrategy;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "samlIdPAuthenticationServiceSelectionStrategyConfiguration", proxyBeanMethods = false)
public class SamlIdPAuthenticationServiceSelectionStrategyConfiguration {

    @Configuration(value = "SamlIdPAuthenticationServiceSelectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPAuthenticationServiceSelectionConfiguration {
        @ConditionalOnMissingBean(name = "samlIdPEntityIdValidationServiceSelectionStrategy")
        @Bean
        @Autowired
        public AuthenticationServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy(
            final CasConfigurationProperties casProperties,
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new SamlIdPEntityIdAuthenticationServiceSelectionStrategy(servicesManager,
                samlIdPServiceFactory, casProperties.getServer().getPrefix());
        }

        @Bean
        @ConditionalOnMissingBean(name = "samlIdPAuthenticationServiceSelectionStrategyConfigurer")
        public AuthenticationServiceSelectionStrategyConfigurer samlIdPAuthenticationServiceSelectionStrategyConfigurer(
            @Qualifier("samlIdPEntityIdValidationServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy) {
            return plan -> plan.registerStrategy(samlIdPEntityIdValidationServiceSelectionStrategy);
        }
    }

    @Configuration(value = "SamlIdPAuthenticationServiceFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class SamlIdPAuthenticationServiceFactoryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlIdPServiceFactory")
        public ServiceFactory samlIdPServiceFactory() {
            return new SamlIdPServiceFactory();
        }

        @Bean
        public ServiceFactoryConfigurer samlIdPServiceFactoryConfigurer(
            @Qualifier("samlIdPServiceFactory")
            final ServiceFactory samlIdPServiceFactory) {
            return () -> CollectionUtils.wrap(samlIdPServiceFactory);
        }
    }
}
