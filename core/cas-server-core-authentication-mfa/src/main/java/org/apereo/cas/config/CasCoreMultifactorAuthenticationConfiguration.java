package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.MultifactorTriggerSelectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.RequestedContextValidator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.apache.commons.lang3.tuple.Pair;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

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
    private ObjectProvider<MultifactorTriggerSelectionStrategy> multifactorTriggerSelectionStrategy;

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
    public RequestedContextValidator<MultifactorAuthenticationProvider> requestedContextValidator() {
        return (assertion, request) -> {
            LOGGER.debug("Locating the primary authentication associated with this service request [{}]", assertion.getService());
            val service = servicesManager.getIfAvailable().findServiceBy(assertion.getService());
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(assertion.getService(), service);
            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
            val authentication = assertion.getPrimaryAuthentication();
            val requestedContext = multifactorTriggerSelectionStrategy.getIfAvailable().resolve(providers.values(), request, service, authentication);

            if (requestedContext.isEmpty()) {
                LOGGER.debug("No particular authentication context is required for this request");
                return Pair.of(Boolean.TRUE, Optional.empty());
            }

            return authenticationContextValidator().validate(authentication, requestedContext.get(), service);
        };
    }

}
