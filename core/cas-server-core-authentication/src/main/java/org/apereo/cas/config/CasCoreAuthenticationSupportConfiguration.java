package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

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
import org.springframework.context.annotation.Lazy;

/**
 * This is {@link CasCoreAuthenticationSupportConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casCoreAuthenticationSupportConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreAuthenticationSupportConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("principalElectionStrategy")
    private ObjectProvider<PrincipalElectionStrategy> principalElectionStrategy;

    @Autowired
    @Qualifier("authenticationTransactionManager")
    private ObjectProvider<AuthenticationTransactionManager> authenticationTransactionManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "authenticationContextValidator")
    public AuthenticationContextValidator authenticationContextValidator() {
        val mfa = casProperties.getAuthn().getMfa();
        val contextAttribute = mfa.getAuthenticationContextAttribute();
        val failureMode = mfa.getGlobalFailureMode();
        val authnAttributeName = mfa.getTrusted().getAuthenticationContextAttribute();
        return new DefaultMultifactorAuthenticationContextValidator(contextAttribute, failureMode, authnAttributeName, applicationContext);
    }

    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport() {
        return new DefaultAuthenticationSystemSupport(authenticationTransactionManager.getIfAvailable(),
            principalElectionStrategy.getIfAvailable());
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        return new RegisteredServiceAuthenticationHandlerResolver(servicesManager.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "authenticationHandlerResolversExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer authenticationHandlerResolversExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerResolver(registeredServiceAuthenticationHandlerResolver());
    }
}
