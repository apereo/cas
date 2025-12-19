package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.LdapUserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.StaticUserGraphicalAuthenticationRepository;
import org.apereo.cas.util.LdapConnectionFactory;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationAction;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.Resource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasGraphicalUserAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "gua")
@AutoConfiguration
public class CasGraphicalUserAuthenticationAutoConfiguration {

    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new GraphicalUserAuthenticationWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "userGraphicalAuthenticationRepository")
    public UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository(
        final CasConfigurationProperties casProperties) {
        val gua = casProperties.getAuthn().getGua();
        if (!gua.getSimple().isEmpty()) {
            val accounts = gua.getSimple().entrySet().stream().map(Unchecked.function(entry -> {
                val res = ResourceUtils.getResourceFrom(entry.getValue());
                return Pair.of(entry.getKey(), (Resource) res);
            })).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            return new StaticUserGraphicalAuthenticationRepository(accounts);
        }
        val ldap = gua.getLdap();
        if (StringUtils.isNotBlank(ldap.getLdapUrl()) && StringUtils.isNotBlank(ldap.getSearchFilter())
            && StringUtils.isNotBlank(ldap.getBaseDn()) && StringUtils.isNotBlank(ldap.getImageAttribute())) {
            val connectionFactory = new LdapConnectionFactory(LdapUtils.newLdaptiveConnectionFactory(gua.getLdap()));
            return new LdapUserGraphicalAuthenticationRepository(casProperties, connectionFactory);
        }
        throw new BeanCreationException("A repository instance must be configured to locate user-defined graphics");
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GUA_ACCEPT_USER)
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action acceptUserGraphicsForAuthenticationAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(AcceptUserGraphicsForAuthenticationAction::new)
            .withId(CasWebflowConstants.ACTION_ID_GUA_ACCEPT_USER)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GUA_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION)
    public Action displayUserGraphicsBeforeAuthenticationAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("userGraphicalAuthenticationRepository")
        final UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> new DisplayUserGraphicsBeforeAuthenticationAction(userGraphicalAuthenticationRepository))
            .withId(CasWebflowConstants.ACTION_ID_GUA_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GUA_PREPARE_LOGIN)
    public Action prepareForGraphicalAuthenticationAction(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(PrepareForGraphicalAuthenticationAction::new)
            .withId(CasWebflowConstants.ACTION_ID_GUA_PREPARE_LOGIN)
            .build()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer(
        @Qualifier("graphicalUserAuthenticationWebflowConfigurer")
        final CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(graphicalUserAuthenticationWebflowConfigurer);
    }
}
