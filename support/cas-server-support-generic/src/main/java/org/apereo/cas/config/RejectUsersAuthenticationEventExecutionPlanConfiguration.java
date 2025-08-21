package org.apereo.cas.config;

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
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link RejectUsersAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "generic")
@Configuration(value = "RejectUsersAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class RejectUsersAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "rejectPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory rejectUsersPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AuthenticationHandler rejectUsersAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("rejectUsersPrincipalFactory")
        final PrincipalFactory rejectUsersPrincipalFactory,
        @Qualifier("rejectPasswordPolicyConfiguration")
        final PasswordPolicyContext rejectPasswordPolicyConfiguration,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val rejectProperties = casProperties.getAuthn().getReject();
        val users = org.springframework.util.StringUtils.commaDelimitedListToSet(rejectProperties.getUsers());
        val h = new RejectUsersAuthenticationHandler(rejectProperties.getName(), rejectUsersPrincipalFactory, users);
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rejectProperties.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(rejectPasswordPolicyConfiguration);
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(rejectProperties.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "rejectUsersAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer rejectUsersAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("rejectUsersAuthenticationHandler")
        final AuthenticationHandler rejectUsersAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val users = casProperties.getAuthn().getReject().getUsers();
            if (StringUtils.isNotBlank(users)) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(rejectUsersAuthenticationHandler, defaultPrincipalResolver);
                LOGGER.debug("Added rejecting authentication handler with the following users [{}]", users);
            }
        };
    }

    @ConditionalOnMissingBean(name = "rejectPasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext rejectPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
