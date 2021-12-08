package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "CasCoreAuthenticationServiceSelectionStrategyConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationServiceSelectionStrategyConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "casCoreAuthenticationServiceSelectionStrategyConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationServiceSelectionStrategyConfigurer casCoreAuthenticationServiceSelectionStrategyConfigurer() {
        return plan -> plan.registerStrategy(new DefaultAuthenticationServiceSelectionStrategy());
    }
}
