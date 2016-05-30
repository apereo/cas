package org.apereo.cas.adaptors.trusted.config;

import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link TrustedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("trustedAuthenticationConfiguration")
public class TrustedAuthenticationConfiguration {
    
    @Bean
    @RefreshScope
    public AuthenticationHandler principalBearingCredentialsAuthenticationHandler() {
        return new PrincipalBearingCredentialsAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver trustedPrincipalResolver() {
        return new PrincipalBearingPrincipalResolver();
    }


    @Bean
    @RefreshScope
    public Action principalFromRemoteUserAction() {
        return new PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction();
    }
    
    @Bean
    @RefreshScope
    public Action principalFromRemoteUserPrincipalAction() {
        return new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction();
    }
}
