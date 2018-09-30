package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreMultifactorAuthenticationConfiguration}.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Configuration("casCoreMultifactorAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreMultifactorAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "authenticationContextValidator")
    public MultifactorAuthenticationContextValidator authenticationContextValidator() {
        val mfa = casProperties.getAuthn().getMfa();
        val contextAttribute = mfa.getAuthenticationContextAttribute();
        val failureMode = mfa.getGlobalFailureMode();
        val authnAttributeName = mfa.getTrusted().getAuthenticationContextAttribute();
        return new DefaultMultifactorAuthenticationContextValidator(contextAttribute, failureMode, authnAttributeName, applicationContext);
    }
}
