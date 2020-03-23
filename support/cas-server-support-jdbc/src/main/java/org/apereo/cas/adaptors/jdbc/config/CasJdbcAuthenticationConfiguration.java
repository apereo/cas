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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link CasJdbcAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("CasJdbcAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasJdbcAuthenticationConfiguration {
    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "jdbcAuthenticationHandlers")
    @Bean
    @RefreshScope
    public Collection<AuthenticationHandler> jdbcAuthenticationHandlers() {
        val handlers = new HashSet<AuthenticationHandler>();
        val jdbc = casProperties.getAuthn().getJdbc();
        jdbc.getBind().forEach(b -> handlers.add(bindModeSearchDatabaseAuthenticationHandler(b)));
        jdbc.getEncode().forEach(b -> handlers.add(queryAndEncodeDatabaseAuthenticationHandler(b)));
        jdbc.getQuery().forEach(b -> handlers.add(queryDatabaseAuthenticationHandler(b)));
        jdbc.getSearch().forEach(b -> handlers.add(searchModeSearchDatabaseAuthenticationHandler(b)));
        return handlers;
    }

    @ConditionalOnMissingBean(name = "jdbcPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory jdbcPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "queryAndEncodePasswordPolicyConfiguration")
    @Bean
    @RefreshScope
    public PasswordPolicyContext queryAndEncodePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "searchModePasswordPolicyConfiguration")
    @Bean
    @RefreshScope
    public PasswordPolicyContext searchModePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "queryPasswordPolicyConfiguration")
    @Bean
    @RefreshScope
    public PasswordPolicyContext queryPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "bindSearchPasswordPolicyConfiguration")
    @Bean
    @RefreshScope
    public PasswordPolicyContext bindSearchPasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }

    @ConditionalOnMissingBean(name = "jdbcAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer jdbcAuthenticationEventExecutionPlanConfigurer() {
        return plan -> jdbcAuthenticationHandlers()
            .forEach(h -> plan.registerAuthenticationHandlerWithPrincipalResolver(h, defaultPrincipalResolver.getObject()));
    }

    private AuthenticationHandler bindModeSearchDatabaseAuthenticationHandler(final BindJdbcAuthenticationProperties b) {
        val h = new BindModeSearchDatabaseAuthenticationHandler(b.getName(), servicesManager.getObject(),
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b));
        configureJdbcAuthenticationHandler(h, b);
        return h;
    }

    private AuthenticationHandler queryAndEncodeDatabaseAuthenticationHandler(final QueryEncodeJdbcAuthenticationProperties b) {
        val h = new QueryAndEncodeDatabaseAuthenticationHandler(b.getName(), servicesManager.getObject(),
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b), b.getAlgorithmName(), b.getSql(), b.getPasswordFieldName(),
            b.getSaltFieldName(), b.getExpiredFieldName(), b.getDisabledFieldName(), b.getNumberOfIterationsFieldName(), b.getNumberOfIterations(),
            b.getStaticSalt());

        configureJdbcAuthenticationHandler(h, b);
        return h;
    }

    private AuthenticationHandler queryDatabaseAuthenticationHandler(final QueryJdbcAuthenticationProperties b) {
        val attributes = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(b.getPrincipalAttributeList());
        LOGGER.trace("Created and mapped principal attributes [{}] for [{}]...", attributes, b.getUrl());

        val h = new QueryDatabaseAuthenticationHandler(b.getName(), servicesManager.getObject(),
            jdbcPrincipalFactory(), b.getOrder(),
            JpaBeans.newDataSource(b), b.getSql(), b.getFieldPassword(),
            b.getFieldExpired(), b.getFieldDisabled(), CollectionUtils.wrap(attributes));

        configureJdbcAuthenticationHandler(h, b);
        h.setPasswordPolicyConfiguration(queryPasswordPolicyConfiguration());
        return h;
    }

    private AuthenticationHandler searchModeSearchDatabaseAuthenticationHandler(final SearchJdbcAuthenticationProperties b) {
        val h = new SearchModeSearchDatabaseAuthenticationHandler(b.getName(), servicesManager.getObject(),
            jdbcPrincipalFactory(), b.getOrder(), JpaBeans.newDataSource(b),
            b.getFieldUser(), b.getFieldPassword(), b.getTableUsers());
        configureJdbcAuthenticationHandler(h, b);
        return h;
    }

    private void configureJdbcAuthenticationHandler(final AbstractJdbcUsernamePasswordAuthenticationHandler handler,
                                                    final BaseJdbcAuthenticationProperties properties) {
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(properties.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(properties.getPrincipalTransformation()));
        handler.setPasswordPolicyConfiguration(bindSearchPasswordPolicyConfiguration());

        if (StringUtils.isNotBlank(properties.getCredentialCriteria())) {
            handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(properties.getCredentialCriteria()));
        }
        LOGGER.trace("Configured authentication handler [{}] to handle database url at [{}]", handler.getName(), properties.getUrl());
    }
}
