package org.apereo.cas.config;

import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link ShiroAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "shiroAuthenticationConfiguration", proxyBeanMethods = false)
public class ShiroAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "shiroPrincipalFactory")
    @Bean
    public PrincipalFactory shiroPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "shiroAuthenticationHandler")
    @Autowired
    public AuthenticationHandler shiroAuthenticationHandler(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier("shiroPrincipalFactory")
        final PrincipalFactory shiroPrincipalFactory,
        @Qualifier("shiroPasswordPolicyConfiguration")
        final PasswordPolicyContext shiroPasswordPolicyConfiguration,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val shiro = casProperties.getAuthn().getShiro();
        val h = new ShiroAuthenticationHandler(shiro.getName(), servicesManager, shiroPrincipalFactory, shiro.getRequiredRoles(), shiro.getRequiredPermissions());
        h.loadShiroConfiguration(shiro.getLocation());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(shiro.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration);
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(shiro.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "shiroAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer shiroAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("shiroAuthenticationHandler")
        final AuthenticationHandler shiroAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val shiroConfigFile = casProperties.getAuthn()
                .getShiro()
                .getLocation();
            if (shiroConfigFile!=null) {
                LOGGER.debug("Injecting shiro authentication handler configured at [{}]", shiroConfigFile.getDescription());
                plan.registerAuthenticationHandlerWithPrincipalResolver(shiroAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }

    @ConditionalOnMissingBean(name = "shiroPasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyContext shiroPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
