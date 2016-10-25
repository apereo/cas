package org.apereo.cas.adaptors.generic.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressAuthenticationHandler;
import org.apereo.cas.adaptors.generic.remote.RemoteAddressNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link CasGenericConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGenericConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasGenericConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasGenericConfiguration.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("shiroPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration shiroPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("rejectPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration rejectPasswordPolicyConfiguration;

    @Autowired(required = false)
    @Qualifier("filePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration filePasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

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
        final RemoteAddressNonInteractiveCredentialsAction a = new RemoteAddressNonInteractiveCredentialsAction();
        a.setAdaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy);
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
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

        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getFile().getPasswordEncoder()));
        if (filePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getFile().getPrincipalTransformation()));


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
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getReject().getPasswordEncoder()));
        if (rejectPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getReject().getPrincipalTransformation()));

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
        h.setPasswordEncoder(Beans.newPasswordEncoder(casProperties.getAuthn().getShiro().getPasswordEncoder()));
        if (shiroPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(casProperties.getAuthn().getShiro().getPrincipalTransformation()));
        return h;
    }

    @Bean
    public PrincipalFactory remoteAddressPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        if (casProperties.getAuthn().getShiro().getConfig().getLocation() != null) {
            LOGGER.debug("Injecting shiro authentication handler");
            this.authenticationHandlersResolvers.put(shiroAuthenticationHandler(), personDirectoryPrincipalResolver);
        }

        if (StringUtils.isNotBlank(casProperties.getAuthn().getReject().getUsers())) {
            LOGGER.debug("Added rejecting authentication handler");
            this.authenticationHandlersResolvers.put(rejectUsersAuthenticationHandler(), personDirectoryPrincipalResolver);
        }

        if (casProperties.getAuthn().getFile().getFilename() != null) {
            LOGGER.debug("Added file-based authentication handler");
            this.authenticationHandlersResolvers.put(fileAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }

}
