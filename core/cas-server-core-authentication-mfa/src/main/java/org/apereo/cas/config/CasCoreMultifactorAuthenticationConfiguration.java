package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.DefaultRequestedAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasCoreMultifactorAuthenticationConfiguration}.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Configuration(value = "casCoreMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfigureAfter(CasCoreServicesConfiguration.class)
public class CasCoreMultifactorAuthenticationConfiguration {

    @Configuration(value = "CasCoreMultifactorAuthenticationContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreMultifactorAuthenticationContextConfiguration {
        
        @Bean
        @ConditionalOnMissingBean(name = "requestedContextValidator")
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RequestedAuthenticationContextValidator requestedContextValidator(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("defaultMultifactorTriggerSelectionStrategy")
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier("authenticationContextValidator")
            final MultifactorAuthenticationContextValidator authenticationContextValidator) {
            return new DefaultRequestedAuthenticationContextValidator(servicesManager,
                multifactorTriggerSelectionStrategy, authenticationContextValidator);
        }
    }

    @Configuration(value = "CasCoreMultifactorAuthenticationFailureConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreMultifactorAuthenticationFailureConfiguration {
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "authenticationContextValidator")
        public MultifactorAuthenticationContextValidator authenticationContextValidator(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val mfa = casProperties.getAuthn().getMfa();
            val contextAttribute = mfa.getCore().getAuthenticationContextAttribute();
            val authnAttributeName = mfa.getTrusted().getCore().getAuthenticationContextAttribute();
            return new DefaultMultifactorAuthenticationContextValidator(contextAttribute, authnAttributeName, applicationContext);
        }

        
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "failureModeEvaluator")
        @Autowired
        public MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator(final CasConfigurationProperties casProperties) {
            return new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        }
    }
}
