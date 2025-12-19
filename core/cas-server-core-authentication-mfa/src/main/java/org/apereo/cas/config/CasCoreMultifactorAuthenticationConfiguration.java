package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.DefaultRequestedAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication)
@Configuration(value = "CasCoreMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
class CasCoreMultifactorAuthenticationConfiguration {

    @Configuration(value = "CasCoreMultifactorAuthenticationContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreMultifactorAuthenticationContextConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "requestedContextValidator")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public RequestedAuthenticationContextValidator requestedContextValidator(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(MultifactorAuthenticationTriggerSelectionStrategy.BEAN_NAME)
            final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
            @Qualifier(MultifactorAuthenticationContextValidator.BEAN_NAME)
            final MultifactorAuthenticationContextValidator authenticationContextValidator) {
            return new DefaultRequestedAuthenticationContextValidator(servicesManager,
                multifactorTriggerSelectionStrategy, authenticationContextValidator);
        }
    }

    @Configuration(value = "CasCoreMultifactorAuthenticationFailureConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreMultifactorAuthenticationFailureConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = MultifactorAuthenticationContextValidator.BEAN_NAME)
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
        public MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator(final CasConfigurationProperties casProperties) {
            return new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        }
    }
}
