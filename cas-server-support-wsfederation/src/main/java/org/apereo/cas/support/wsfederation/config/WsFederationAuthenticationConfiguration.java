package org.apereo.cas.support.wsfederation.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.wsfederation.WsFedApplicationContextWrapper;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.flow.WsFederationAction;
import org.apereo.cas.web.BaseApplicationContextWrapper;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link WsFederationAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("wsFederationConfiguration")
public class WsFederationAuthenticationConfiguration {
    @Bean
    public BaseApplicationContextWrapper wsFedApplicationContextWrapper() {
        return new WsFedApplicationContextWrapper();
    }
    
    @Bean
    @RefreshScope
    public WsFederationConfiguration wsFedConfig() {
        return new WsFederationConfiguration();
    }
    
    @Bean
    @RefreshScope
    public WsFederationHelper wsFederationHelper() {
        return new WsFederationHelper();
    }
    
    @Bean
    @RefreshScope
    public AuthenticationHandler adfsAuthNHandler() {
        return new WsFederationAuthenticationHandler();
    }

    @Bean
    @RefreshScope
    public PrincipalResolver adfsPrincipalResolver() {
        return new WsFederationCredentialsToPrincipalResolver();
    }

    @Bean
    @RefreshScope
    public Action wsFederationAction() {
        return new WsFederationAction();
    }
}
