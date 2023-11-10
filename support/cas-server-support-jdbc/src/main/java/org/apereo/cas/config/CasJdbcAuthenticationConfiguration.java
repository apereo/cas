package org.apereo.cas.config;

import org.apereo.cas.adaptors.jdbc.JdbcAuthenticationUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link CasJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "jdbc")
@AutoConfiguration
public class CasJdbcAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "queryAndEncodeDatabaseAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> queryAndEncodeDatabaseAuthenticationHandlers(
        @Qualifier("queryAndEncodePasswordPolicyConfiguration") final PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("jdbcPrincipalFactory") final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getEncode().forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
                jdbcPrincipalFactory, servicesManager, queryAndEncodePasswordPolicyConfiguration);
            handlers.add(handler);
        });
        return handlers;
    }

    @ConditionalOnMissingBean(name = "bindModeSearchDatabaseAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> bindModeSearchDatabaseAuthenticationHandlers(
        @Qualifier("bindSearchPasswordPolicyConfiguration") final PasswordPolicyContext bindSearchPasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("jdbcPrincipalFactory") final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getBind().forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext,
                jdbcPrincipalFactory, servicesManager, bindSearchPasswordPolicyConfiguration);
            handlers.add(handler);
        });
        return handlers;
    }

    @ConditionalOnMissingBean(name = "queryDatabaseAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> queryDatabaseAuthenticationHandlers(
        @Qualifier("queryPasswordPolicyConfiguration") final PasswordPolicyContext queryPasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("jdbcPrincipalFactory") final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getQuery().forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext, jdbcPrincipalFactory,
                servicesManager, queryPasswordPolicyConfiguration);
            handlers.add(handler);
        });
        return handlers;
    }

    @ConditionalOnMissingBean(name = "searchModeSearchDatabaseAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> searchModeSearchDatabaseAuthenticationHandlers(
        @Qualifier("searchModePasswordPolicyConfiguration") final PasswordPolicyContext searchModePasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
        @Qualifier("jdbcPrincipalFactory") final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getSearch().forEach(properties -> {
            val handler = JdbcAuthenticationUtils.newAuthenticationHandler(properties, applicationContext, jdbcPrincipalFactory,
                servicesManager, searchModePasswordPolicyConfiguration);
            handlers.add(handler);
        });
        return handlers;
    }

    @ConditionalOnMissingBean(name = "jdbcAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Collection<AuthenticationHandler> jdbcAuthenticationHandlers(
        @Qualifier("queryAndEncodeDatabaseAuthenticationHandlers") final Collection<AuthenticationHandler> queryAndEncodeDatabaseAuthenticationHandlers,
        @Qualifier("bindModeSearchDatabaseAuthenticationHandlers") final Collection<AuthenticationHandler> bindModeSearchDatabaseAuthenticationHandlers,
        @Qualifier("queryDatabaseAuthenticationHandlers") final Collection<AuthenticationHandler> queryDatabaseAuthenticationHandlers,
        @Qualifier("searchModeSearchDatabaseAuthenticationHandlers") final Collection<AuthenticationHandler> searchModeSearchDatabaseAuthenticationHandlers) {
        val handlers = new HashSet<AuthenticationHandler>();
        handlers.addAll(bindModeSearchDatabaseAuthenticationHandlers);
        handlers.addAll(queryAndEncodeDatabaseAuthenticationHandlers);
        handlers.addAll(queryDatabaseAuthenticationHandlers);
        handlers.addAll(searchModeSearchDatabaseAuthenticationHandlers);
        return handlers;
    }

    @ConditionalOnMissingBean(name = "jdbcPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory jdbcPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "queryAndEncodePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "searchModePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext searchModePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "queryPasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext queryPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "bindSearchPasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext bindSearchPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "jdbcAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer jdbcAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("jdbcAuthenticationHandlers") final Collection<AuthenticationHandler> jdbcAuthenticationHandlers,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER) final PrincipalResolver defaultPrincipalResolver) {
        return plan -> jdbcAuthenticationHandlers.forEach(h ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(h, defaultPrincipalResolver));
    }
}
