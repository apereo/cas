package org.apereo.cas.adaptors.generic.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RemoteAddressAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
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
@EnableConfigurationProperties({
        RemoteAddressAuthenticationProperties.class,
        ShiroAuthenticationProperties.class})
public class CasGenericConfiguration {

    @Autowired
    private RemoteAddressAuthenticationProperties remoteAuthnProps;

    @Autowired
    private ShiroAuthenticationProperties shiroAuthnProps;

    @Autowired
    private FileAuthenticationProperties fileAuthenticationProperties;

    @Autowired
    private RejectAuthenticationProperties rejectAuthenticationProperties;
    
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
        final FileAuthenticationHandler h = new FileAuthenticationHandler();
        
        h.setFileName(fileAuthenticationProperties.getFilename());
        h.setSeparator(fileAuthenticationProperties.getSeparator());
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        final RejectUsersAuthenticationHandler h = new RejectUsersAuthenticationHandler();

        if (StringUtils.isNotBlank(rejectAuthenticationProperties.getUsers())) {
            h.setUsers(org.springframework.util.StringUtils.commaDelimitedListToSet(
                    rejectAuthenticationProperties.getUsers()));
        }
        
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        final ShiroAuthenticationHandler bean = new ShiroAuthenticationHandler();
        bean.setShiroConfiguration(this.shiroAuthnProps.getConfig().getLocation());
        bean.setRequiredRoles(this.shiroAuthnProps.getRequiredRoles());
        bean.setRequiredPermissions(this.shiroAuthnProps.getRequiredPermissions());
        return bean;
    }
}
