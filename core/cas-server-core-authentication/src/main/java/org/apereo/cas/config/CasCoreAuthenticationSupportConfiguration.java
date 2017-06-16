package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.AuthenticationExceptionHandlerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.webflow.execution.Action;

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
    private ServicesManager servicesManager;
    
    @ConditionalOnMissingBean(name = "authenticationExceptionHandler")
    @Bean
    public Action authenticationExceptionHandler() {
        final AuthenticationExceptionHandlerAction h = new AuthenticationExceptionHandlerAction();
        h.setErrors(casProperties.getAuthn().getExceptions().getExceptions());
        return h;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "authenticationContextValidator")
    public AuthenticationContextValidator authenticationContextValidator() {
        final String contextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        final String failureMode = casProperties.getAuthn().getMfa().getGlobalFailureMode();
        final String authnAttributeName = casProperties.getAuthn().getMfa().getTrusted().getAuthenticationContextAttribute();
        return new DefaultAuthenticationContextValidator(contextAttribute, failureMode, authnAttributeName);
    }

    @Bean
    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver() {
        return new RegisteredServiceAuthenticationHandlerResolver(servicesManager);
    }
    
    @Autowired
    @Bean
    public AuthenticationSystemSupport defaultAuthenticationSystemSupport(@Qualifier("principalElectionStrategy")
                                                                          final PrincipalElectionStrategy principalElectionStrategy,
                                                                          @Qualifier("authenticationTransactionManager")
                                                                          final AuthenticationTransactionManager authenticationTransactionManager) {
        return new DefaultAuthenticationSystemSupport(authenticationTransactionManager, principalElectionStrategy);
    }
}
