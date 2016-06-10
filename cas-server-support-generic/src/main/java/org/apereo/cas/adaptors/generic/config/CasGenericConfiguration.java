package org.apereo.cas.adaptors.generic.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CasGenericConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;


    @Autowired(required=false)
    @Qualifier("shiroPasswordEncoder")
    private PasswordEncoder shiroPasswordEncoder;

    @Autowired(required=false)
    @Qualifier("shiroPrincipalNameTransformer")
    private PrincipalNameTransformer shiroPrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("shiroPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration shiroPasswordPolicyConfiguration;
    
    @Autowired(required=false)
    @Qualifier("rejectPasswordEncoder")
    private PasswordEncoder rejectPasswordEncoder;

    @Autowired(required=false)
    @Qualifier("rejectPrincipalNameTransformer")
    private PrincipalNameTransformer rejectPrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("rejectPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration rejectPasswordPolicyConfiguration;
    
    @Autowired(required=false)
    @Qualifier("filePasswordEncoder")
    private PasswordEncoder filePasswordEncoder;

    @Autowired(required=false)
    @Qualifier("filePrincipalNameTransformer")
    private PrincipalNameTransformer filePrincipalNameTransformer;

    @Autowired(required=false)
    @Qualifier("filePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration filePasswordPolicyConfiguration;
    
    @Bean
    @RefreshScope
    public AuthenticationHandler remoteAddressAuthenticationHandler() {
        final RemoteAddressAuthenticationHandler bean = new RemoteAddressAuthenticationHandler();
        bean.setIpNetworkRange(casProperties.getAuthn().getRemoteAddress().getIpAddressRange());
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

        h.setFileName(casProperties.getAuthn().getFile().getFilename());
        h.setSeparator(casProperties.getAuthn().getFile().getSeparator());


        if (filePasswordEncoder != null) {
            h.setPasswordEncoder(filePasswordEncoder);
        }
        if (filePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
        }
        if (filePrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(filePrincipalNameTransformer);
        }
        
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        final RejectUsersAuthenticationHandler h = new RejectUsersAuthenticationHandler();

        if (StringUtils.isNotBlank(casProperties.getAuthn().getReject().getUsers())) {
            h.setUsers(org.springframework.util.StringUtils.commaDelimitedListToSet(
                    casProperties.getAuthn().getReject().getUsers()));
        }

        if (rejectPasswordEncoder != null) {
            h.setPasswordEncoder(rejectPasswordEncoder);
        }
        if (rejectPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration);
        }
        if (rejectPrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(rejectPrincipalNameTransformer);
        }
        
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        final ShiroAuthenticationHandler h = new ShiroAuthenticationHandler();
        h.setShiroConfiguration(casProperties.getAuthn().getShiro().getConfig().getLocation());
        h.setRequiredRoles(casProperties.getAuthn().getShiro().getRequiredRoles());
        h.setRequiredPermissions(casProperties.getAuthn().getShiro().getRequiredPermissions());

        if (shiroPasswordEncoder != null) {
            h.setPasswordEncoder(shiroPasswordEncoder);
        }
        if (shiroPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration);
        }
        if (shiroPrincipalNameTransformer != null) {
            h.setPrincipalNameTransformer(shiroPrincipalNameTransformer);
        }
        
        return h;
    }
}
