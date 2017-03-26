package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.ShiroAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link ShiroAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("shiroAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class ShiroAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroAuthenticationEventExecutionPlanConfiguration.class);

    @Autowired(required = false)
    @Qualifier("shiroPasswordPolicyConfiguration")
    private PasswordPolicyConfiguration shiroPasswordPolicyConfiguration;
    
    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "shiroPrincipalFactory")
    @Bean
    public PrincipalFactory shiroPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }


    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        final ShiroAuthenticationProperties shiro = casProperties.getAuthn().getShiro();
        final ShiroAuthenticationHandler h = new ShiroAuthenticationHandler(shiro.getName(), servicesManager, shiroPrincipalFactory(),
                shiro.getRequiredRoles(), shiro.getRequiredPermissions());

        h.loadShiroConfiguration(shiro.getConfig().getLocation());
        h.setPasswordEncoder(Beans.newPasswordEncoder(shiro.getPasswordEncoder()));
        if (shiroPasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(shiro.getPrincipalTransformation()));
        return h;
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (casProperties.getAuthn().getShiro().getConfig().getLocation() != null) {
            LOGGER.debug("Injecting shiro authentication handler");
            plan.registerAuthenticationHandlerWithPrincipalResolver(shiroAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
}
