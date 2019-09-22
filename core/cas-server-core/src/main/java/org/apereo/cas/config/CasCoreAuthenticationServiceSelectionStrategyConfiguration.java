package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreAuthenticationServiceSelectionStrategyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationServiceSelectionStrategyConfiguration {

    @Bean
    public AuthenticationServiceSelectionStrategyConfigurer casCoreAuthenticationServiceSelectionStrategyConfigurer() {
        return plan -> plan.registerStrategy(new DefaultAuthenticationServiceSelectionStrategy());
    }
}
