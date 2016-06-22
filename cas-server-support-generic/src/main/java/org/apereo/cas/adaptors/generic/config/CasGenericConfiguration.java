package org.apereo.cas.adaptors.generic.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasGenericConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGenericConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasGenericConfiguration {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired(required = false)
    @Qualifier("shiroPasswordEncoder")
    private PasswordEncoder shiroPasswordEncoder;

    @Autowired(required = false)
    @Qualifier("shiroPrincipalNameTransformer")
    private PrincipalNameTransformer shiroPrincipalNameTransformer;

    @Autowired(required = false)
    @Qualifier("shiroPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration shiroPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("rejectPasswordEncoder")
    private PasswordEncoder rejectPasswordEncoder;

    @Autowired(required = false)
    @Qualifier("rejectPrincipalNameTransformer")
    private PrincipalNameTransformer rejectPrincipalNameTransformer;

    @Autowired(required = false)
    @Qualifier("rejectPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration rejectPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("filePasswordEncoder")
    private PasswordEncoder filePasswordEncoder;

    @Autowired(required = false)
    @Qualifier("filePrincipalNameTransformer")
    private PrincipalNameTransformer filePrincipalNameTransformer;

    @Autowired(required = false)
    @Qualifier("filePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration filePasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;


    @Bean
    @RefreshScope
    public AuthenticationHandler remoteAddressAuthenticationHandler() {
        final RemoteAddressAuthenticationHandler bean = new RemoteAddressAuthenticationHandler();
        bean.setIpNetworkRange(casProperties.getAuthn().getRemoteAddress().getIpAddressRange());
        bean.setPrincipalFactory(remoteAddressPrincipalFactory());
        bean.setServicesManager(servicesManager);
        return bean;
    }

    @Bean
    public Action remoteAddressCheck() {
        final RemoteAddressNonInteractiveCredentialsAction a =
                new RemoteAddressNonInteractiveCredentialsAction();
        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setPrincipalFactory(remoteAddressPrincipalFactory());
        a.setWarnCookieGenerator(warnCookieGenerator);
        return a;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler fileAuthenticationHandler() {
        final FileAuthenticationHandler h = new FileAuthenticationHandler();

        h.setFileName(casProperties.getAuthn().getFile().getFilename());
        h.setSeparator(casProperties.getAuthn().getFile().getSeparator());
        h.setPrincipalFactory(filePrincipalFactory());
        h.setServicesManager(servicesManager);

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

    @Bean
    public PrincipalFactory filePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalFactory rejectUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public PrincipalFactory shiroPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        final RejectUsersAuthenticationHandler h = new RejectUsersAuthenticationHandler();
        h.setPrincipalFactory(rejectUsersPrincipalFactory());
        h.setServicesManager(servicesManager);
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

        h.setPrincipalFactory(shiroPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setRequiredRoles(casProperties.getAuthn().getShiro().getRequiredRoles());
        h.setRequiredPermissions(casProperties.getAuthn().getShiro().getRequiredPermissions());
        h.loadShiroConfiguration(casProperties.getAuthn().getShiro().getConfig().getLocation());

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

    @Bean
    public PrincipalFactory remoteAddressPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

}
