package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.model.support.generic.RemoteAddressAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasGenericConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGenericConfiguration")
@EnableConfigurationProperties(RemoteAddressAuthenticationProperties.class)
public class CasGenericConfiguration {

    @Autowired
    RemoteAddressAuthenticationProperties remoteAuthnProps;

    @Bean
    @RefreshScope
    public AuthenticationHandler remoteAddressAuthenticationHandler() {
        final RemoteAddressAuthenticationHandler bean = new RemoteAddressAuthenticationHandler();
        bean.setIpNetworkRange(this.remoteAuthnProps.getIpAddressRange());
        return bean;
    }
    
    @Bean
    public Action remoteAddressCheck() {
        return new RemoteAddressNonInteractiveCredentialsAction();
    }
    
    @RefreshScope
    @Bean
    public AuthenticationHandler fileAuthenticationHandler() {
        return new FileAuthenticationHandler();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        return new RejectUsersAuthenticationHandler();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        return new ShiroAuthenticationHandler();
    }
}
