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
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.RejectAuthenticationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

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
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Bean
    @RefreshScope
    public AuthenticationHandler remoteAddressAuthenticationHandler() {
        final RemoteAddressAuthenticationHandler bean = new RemoteAddressAuthenticationHandler();
        bean.setIpNetworkRange(casProperties.getAuthn().getRemoteAddress().getIpAddressRange());
        bean.setPrincipalFactory(remoteAddressPrincipalFactory());
        bean.setServicesManager(servicesManager);
        bean.setName(casProperties.getAuthn().getRemoteAddress().getName());
        return bean;
    }

    @Bean
    public Action remoteAddressCheck() {
        return new RemoteAddressNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver, 
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy);
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler fileAuthenticationHandler() {
        final FileAuthenticationProperties fileProperties = casProperties.getAuthn().getFile();
        final FileAuthenticationHandler h = new FileAuthenticationHandler(fileProperties.getFilename(), fileProperties.getSeparator());
        h.setPrincipalFactory(filePrincipalFactory());
        h.setServicesManager(servicesManager);

        h.setPasswordEncoder(Beans.newPasswordEncoder(fileProperties.getPasswordEncoder()));
        if (filePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(fileProperties.getPrincipalTransformation()));
        h.setName(fileProperties.getName());

        return h;
    }

    @ConditionalOnMissingBean(name = "filePrincipalFactory")
    @Bean
    public PrincipalFactory filePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "rejectPrincipalFactory")
    @Bean
    public PrincipalFactory rejectUsersPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "shiroPrincipalFactory")
    @Bean
    public PrincipalFactory shiroPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        final RejectAuthenticationProperties rejectProperties = casProperties.getAuthn().getReject();
        final Set<String> users = org.springframework.util.StringUtils.commaDelimitedListToSet(rejectProperties.getUsers());
        final RejectUsersAuthenticationHandler h = new RejectUsersAuthenticationHandler(users);
        h.setPrincipalFactory(rejectUsersPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setPasswordEncoder(Beans.newPasswordEncoder(rejectProperties.getPasswordEncoder()));
        if (rejectPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(rejectProperties.getPrincipalTransformation()));
        h.setName(rejectProperties.getName());
        return h;
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        final ShiroAuthenticationProperties shiro = casProperties.getAuthn().getShiro();
        final ShiroAuthenticationHandler h = new ShiroAuthenticationHandler(shiro.getRequiredRoles(), shiro.getRequiredPermissions());

        h.setPrincipalFactory(shiroPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.loadShiroConfiguration(shiro.getConfig().getLocation());
        h.setPasswordEncoder(Beans.newPasswordEncoder(shiro.getPasswordEncoder()));
        if (shiroPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(shiro.getPrincipalTransformation()));
        h.setName(shiro.getName());
        return h;
    }

    @ConditionalOnMissingBean(name = "remoteAddressPrincipalFactory")
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
