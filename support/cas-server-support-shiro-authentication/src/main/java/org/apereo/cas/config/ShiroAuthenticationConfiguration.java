package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link ShiroAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("shiroAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class ShiroAuthenticationConfiguration {

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
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler shiroAuthenticationHandler() {
        final var shiro = casProperties.getAuthn().getShiro();
        final var h = new ShiroAuthenticationHandler(shiro.getName(), servicesManager, shiroPrincipalFactory(),
            shiro.getRequiredRoles(), shiro.getRequiredPermissions());

        h.loadShiroConfiguration(shiro.getLocation());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(shiro.getPasswordEncoder()));
        h.setPasswordPolicyConfiguration(shiroPasswordPolicyConfiguration());
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(shiro.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "shiroAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer shiroAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            final var shiroConfigFile = casProperties.getAuthn().getShiro().getLocation();
            if (shiroConfigFile != null) {
                LOGGER.debug("Injecting shiro authentication handler configured at [{}]", shiroConfigFile.getDescription());
                plan.registerAuthenticationHandlerWithPrincipalResolver(shiroAuthenticationHandler(), personDirectoryPrincipalResolver);
            }
        };
    }

    @ConditionalOnMissingBean(name = "shiroPasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyConfiguration shiroPasswordPolicyConfiguration() {
        return new PasswordPolicyConfiguration();
    }
}
