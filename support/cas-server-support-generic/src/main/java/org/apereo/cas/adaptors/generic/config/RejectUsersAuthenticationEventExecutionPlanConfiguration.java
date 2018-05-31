package org.apereo.cas.adaptors.generic.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "rejectPrincipalFactory")
    @Bean
    public PrincipalFactory rejectUsersPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler() {
        final var rejectProperties = casProperties.getAuthn().getReject();
        final var users = org.springframework.util.StringUtils.commaDelimitedListToSet(rejectProperties.getUsers());
        final var h = new RejectUsersAuthenticationHandler(rejectProperties.getName(), servicesManager,
            rejectUsersPrincipalFactory(), users);
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rejectProperties.getPasswordEncoder()));
        h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration());
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(rejectProperties.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "rejectUsersAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer rejectUsersAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            final var users = casProperties.getAuthn().getReject().getUsers();
            if (StringUtils.isNotBlank(users)) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(rejectUsersAuthenticationHandler(), personDirectoryPrincipalResolver);
                LOGGER.debug("Added rejecting authentication handler with the following users [{}]", users);
            }
        };
    }

    @ConditionalOnMissingBean(name = "rejectPasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyConfiguration rejectPasswordPolicyConfiguration() {
        return new PasswordPolicyConfiguration();
    }
}
