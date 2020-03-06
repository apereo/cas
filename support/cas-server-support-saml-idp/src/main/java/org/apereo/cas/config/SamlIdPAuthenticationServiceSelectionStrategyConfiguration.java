package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.ServiceFactoryConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.authentication.SamlIdPServiceFactory;
import org.apereo.cas.support.saml.services.SamlIdPEntityIdAuthenticationServiceSelectionStrategy;
import org.apereo.cas.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
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
@Configuration("samlIdPAuthenticationServiceSelectionStrategyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlIdPAuthenticationServiceSelectionStrategyConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "samlIdPEntityIdValidationServiceSelectionStrategy")
    @Bean
    public AuthenticationServiceSelectionStrategy samlIdPEntityIdValidationServiceSelectionStrategy() {
        return new SamlIdPEntityIdAuthenticationServiceSelectionStrategy(samlIdPServiceFactory(),
            casProperties.getServer().getPrefix());
    }

    @Bean
    public AuthenticationServiceSelectionStrategyConfigurer samlIdPAuthenticationServiceSelectionStrategyConfigurer() {
        return plan -> plan.registerStrategy(samlIdPEntityIdValidationServiceSelectionStrategy());
    }

    @Bean
    public ServiceFactory samlIdPServiceFactory() {
        return new SamlIdPServiceFactory();
    }

    @Bean
    public ServiceFactoryConfigurer samlIdPServiceFactoryConfigurer() {
        return () -> CollectionUtils.wrap(samlIdPServiceFactory());
    }
}
