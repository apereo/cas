package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.jdbc.JdbcAuthenticationUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link CasJdbcQueryEncodeAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "jdbc")
@Configuration(value = "CasJdbcQueryEncodeAuthenticationConfiguration", proxyBeanMethods = false)
class CasJdbcQueryEncodeAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "queryAndEncodeDatabaseAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> queryAndEncodeDatabaseAuthenticationHandlers(
        @Qualifier("queryAndEncodePasswordPolicyConfiguration")
        final PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("queryAndEncodePrincipalFactory")
        final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getEncode().forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
                jdbcPrincipalFactory, queryAndEncodePasswordPolicyConfiguration);
            handlers.add(handler);
        });
        return handlers;
    }

    @ConditionalOnMissingBean(name = "queryAndEncodePrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory queryAndEncodePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "queryAndEncodePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "queryAndEncodeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer queryAndEncodeAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("queryAndEncodeDatabaseAuthenticationHandlers")
        final Collection<AuthenticationHandler> queryAndEncodeDatabaseAuthenticationHandlers,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> queryAndEncodeDatabaseAuthenticationHandlers.forEach(h ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(h, defaultPrincipalResolver));
    }
}
