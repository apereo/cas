package org.apereo.cas.adaptors.jdbc.config;

import org.apereo.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.BindJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryJdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.jdbc.authn.SearchJdbcAuthenticationProperties;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * This is {@link CasJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "CasJdbcAuthenticationConfiguration", proxyBeanMethods = false)
public class CasJdbcAuthenticationConfiguration {
    private static AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler(final SearchJdbcAuthenticationProperties b,
                                                                                       final PasswordPolicyContext config,
                                                                                       final ConfigurableApplicationContext applicationContext,
                                                                                       final PrincipalFactory jdbcPrincipalFactory,
                                                                                       final ServicesManager servicesManager) {
        val h = new SearchModeSearchDatabaseAuthenticationHandler(b, servicesManager, jdbcPrincipalFactory, JpaBeans.newDataSource(b));
        configureJdbcAuthenticationHandler(h, config, b, applicationContext);
        return h;
    }

    private static void configureJdbcAuthenticationHandler(final AbstractJdbcUsernamePasswordAuthenticationHandler handler,
                                                           final PasswordPolicyContext config,
                                                           final BaseJdbcAuthenticationProperties properties,
                                                           final ConfigurableApplicationContext applicationContext) {
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(properties.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties.getPrincipalTransformation()));
        handler.setPasswordPolicyConfiguration(config);
        handler.setState(properties.getState());
        if (StringUtils.isNotBlank(properties.getCredentialCriteria())) {
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(properties.getCredentialCriteria()));
        }
        LOGGER.trace("Configured authentication handler [{}] to handle database url at [{}]", handler.getName(), properties.getName());
    }

    private static AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties b,
                                                                                     final PasswordPolicyContext config,
                                                                                     final ConfigurableApplicationContext applicationContext,
                                                                                     final PrincipalFactory jdbcPrincipalFactory,
                                                                                     final ServicesManager servicesManager) {
        val h = new QueryAndEncodeDatabaseAuthenticationHandler(b, servicesManager,
            jdbcPrincipalFactory, JpaBeans.newDataSource(b));
        configureJdbcAuthenticationHandler(h, config, b, applicationContext);
        return h;
    }

    private static AuthenticationHandler queryDatabaseAuthenticationHandler(final QueryJdbcAuthenticationProperties b,
                                                                            final PasswordPolicyContext config,
                                                                            final ConfigurableApplicationContext applicationContext,
                                                                            final PrincipalFactory jdbcPrincipalFactory,
                                                                            final ServicesManager servicesManager) {
        val attributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(b.getPrincipalAttributeList());
        LOGGER.trace("Created and mapped principal attributes [{}] for [{}]...", attributes, b.getName());
        val h = new QueryDatabaseAuthenticationHandler(b, servicesManager, jdbcPrincipalFactory,
            JpaBeans.newDataSource(b), CollectionUtils.wrap(attributes));
        configureJdbcAuthenticationHandler(h, config, b, applicationContext);
        return h;
    }

    private static AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler(final BindJdbcAuthenticationProperties b,
                                                                                     final PasswordPolicyContext config,
                                                                                     final ConfigurableApplicationContext applicationContext,
                                                                                     final PrincipalFactory jdbcPrincipalFactory,
                                                                                     final ServicesManager servicesManager) {
        val h = new BindModeSearchDatabaseAuthenticationHandler(b.getName(), servicesManager,
            jdbcPrincipalFactory, b.getOrder(), JpaBeans.newDataSource(b));
        configureJdbcAuthenticationHandler(h, config, b, applicationContext);
        return h;
    }

    @ConditionalOnMissingBean(name = "jdbcAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Collection<AuthenticationHandler> jdbcAuthenticationHandlers(
        @Qualifier("queryPasswordPolicyConfiguration")
        final PasswordPolicyContext queryPasswordPolicyConfiguration,
        @Qualifier("searchModePasswordPolicyConfiguration")
        final PasswordPolicyContext searchModePasswordPolicyConfiguration,
        @Qualifier("bindSearchPasswordPolicyConfiguration")
        final PasswordPolicyContext bindSearchPasswordPolicyConfiguration,
        @Qualifier("queryAndEncodePasswordPolicyConfiguration")
        final PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("jdbcPrincipalFactory")
        final PrincipalFactory jdbcPrincipalFactory,
        final CasConfigurationProperties casProperties) {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getBind().forEach(b ->
            handlers.add(bindModeSearchDatabaseAuthenticationHandler(b, bindSearchPasswordPolicyConfiguration, applicationContext, jdbcPrincipalFactory, servicesManager)));
        jdbc.getEncode().forEach(b ->
            handlers.add(queryAndEncodeDatabaseAuthenticationHandler(b, queryAndEncodePasswordPolicyConfiguration, applicationContext, jdbcPrincipalFactory, servicesManager)));
        jdbc.getQuery().forEach(b ->
            handlers.add(queryDatabaseAuthenticationHandler(b, queryPasswordPolicyConfiguration, applicationContext, jdbcPrincipalFactory, servicesManager)));
        jdbc.getSearch().forEach(b ->
            handlers.add(searchModeSearchDatabaseAuthenticationHandler(b, searchModePasswordPolicyConfiguration, applicationContext, jdbcPrincipalFactory, servicesManager)));
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
        @Qualifier("jdbcAuthenticationHandlers")
        final Collection<AuthenticationHandler> jdbcAuthenticationHandlers,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> jdbcAuthenticationHandlers.forEach(h ->
            plan.registerAuthenticationHandlerWithPrincipalResolver(h, defaultPrincipalResolver));
    }
}
