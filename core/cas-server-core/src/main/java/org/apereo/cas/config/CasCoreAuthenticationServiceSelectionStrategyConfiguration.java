package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuthenticationServiceSelectionStrategyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreAuthenticationServiceSelectionStrategyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreAuthenticationServiceSelectionStrategyConfiguration implements AuthenticationServiceSelectionStrategyConfigurer {


    @Override
    public void configureAuthenticationServiceSelectionStrategy(final AuthenticationServiceSelectionPlan plan) {
        plan.registerStrategy(new DefaultAuthenticationServiceSelectionStrategy());
    }
}
