package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.DefaultRequestedAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Slf4j
public class CasCoreMultifactorAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultMultifactorTriggerSelectionStrategy")
    private ObjectProvider<MultifactorAuthenticationTriggerSelectionStrategy> multifactorTriggerSelectionStrategy;

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

    @Bean
    @ConditionalOnMissingBean(name = "requestedContextValidator")
    public RequestedAuthenticationContextValidator<MultifactorAuthenticationProvider> requestedContextValidator() {
        return new DefaultRequestedAuthenticationContextValidator(servicesManager.getIfAvailable(),
            multifactorTriggerSelectionStrategy.getIfAvailable(),
            authenticationContextValidator(),
            applicationContext);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "failureModeEvaluator")
    public MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator() {
        return new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
    }
}
