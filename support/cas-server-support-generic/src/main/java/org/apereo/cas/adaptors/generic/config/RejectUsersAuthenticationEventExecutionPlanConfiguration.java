package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link RejectUsersAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("rejectUsersAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RejectUsersAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "rejectPrincipalFactory")
    @Bean
    public PrincipalFactory rejectUsersPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        val rejectProperties = casProperties.getAuthn().getReject();
        val users = org.springframework.util.StringUtils.commaDelimitedListToSet(rejectProperties.getUsers());
        val h = new RejectUsersAuthenticationHandler(rejectProperties.getName(), servicesManager.getObject(),
            rejectUsersPrincipalFactory(), users);
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rejectProperties.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration());
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(rejectProperties.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "rejectUsersAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer rejectUsersAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val users = casProperties.getAuthn().getReject().getUsers();
            if (StringUtils.isNotBlank(users)) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(rejectUsersAuthenticationHandler(), defaultPrincipalResolver.getObject());
                LOGGER.debug("Added rejecting authentication handler with the following users [{}]", users);
            }
        };
    }

    @ConditionalOnMissingBean(name = "rejectPasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyContext rejectPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
