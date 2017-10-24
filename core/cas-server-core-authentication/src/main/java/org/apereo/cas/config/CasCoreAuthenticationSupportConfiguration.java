package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationHandlerResolver;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationTransactionManager;
import org.apereo.cas.authentication.DefaultAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.RegisteredServiceAuthenticationHandlerResolver;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.web.flow.AuthenticationExceptionHandlerAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.execution.Action;

import java.util.LinkedHashSet;
import java.util.Set;

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
    
    @ConditionalOnMissingBean(name = "authenticationExceptionHandler")
    @Bean
    public Action authenticationExceptionHandler() {
        return new AuthenticationExceptionHandlerAction(handledAuthenticationExceptions());
    }

    @RefreshScope
    @Bean
    public Set<Class<? extends Exception>> handledAuthenticationExceptions() {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one failed due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        final Set<Class<? extends Exception>> errors = new LinkedHashSet<>();
        errors.add(javax.security.auth.login.AccountLockedException.class);
        errors.add(javax.security.auth.login.CredentialExpiredException.class);
        errors.add(javax.security.auth.login.AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);

        errors.add(javax.security.auth.login.AccountNotFoundException.class);
        errors.add(javax.security.auth.login.FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);

        errors.addAll(casProperties.getAuthn().getExceptions().getExceptions());
        
        return errors;
    }
    
    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "authenticationContextValidator")
    public AuthenticationContextValidator authenticationContextValidator() {
        final MultifactorAuthenticationProperties mfa = casProperties.getAuthn().getMfa();
        final String contextAttribute = mfa.getAuthenticationContextAttribute();
        final String failureMode = mfa.getGlobalFailureMode();
        final String authnAttributeName = mfa.getTrusted().getAuthenticationContextAttribute();
        return new DefaultAuthenticationContextValidator(contextAttribute, failureMode, authnAttributeName);
    }

    @Autowired
    @Bean
    @Lazy
    @ConditionalOnMissingBean(name = "registeredServiceAuthenticationHandlerResolver")
    public AuthenticationHandlerResolver registeredServiceAuthenticationHandlerResolver(@Qualifier("servicesManager")
                                                                                        final ServicesManager servicesManager) {
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
