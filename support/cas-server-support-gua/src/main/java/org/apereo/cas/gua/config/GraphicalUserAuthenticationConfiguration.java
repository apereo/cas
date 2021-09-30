package org.apereo.cas.gua.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.LdapUserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.StaticUserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationAction;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationAction;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.Resource;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.stream.Collectors;

/**
 * This is {@link GraphicalUserAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "graphicalUserAuthenticationConfiguration", proxyBeanMethods = false)
public class GraphicalUserAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new GraphicalUserAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "userGraphicalAuthenticationRepository")
    @Autowired
    public UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository(final CasConfigurationProperties casProperties) {
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
            val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(gua.getLdap());
            return new LdapUserGraphicalAuthenticationRepository(casProperties, connectionFactory);
        }
        throw new BeanCreationException("A repository instance must be configured to locate user-defined graphics");
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptUserGraphicsForAuthenticationAction")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action acceptUserGraphicsForAuthenticationAction() {
        return new AcceptUserGraphicsForAuthenticationAction();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "displayUserGraphicsBeforeAuthenticationAction")
    public Action displayUserGraphicsBeforeAuthenticationAction(
        @Qualifier("userGraphicalAuthenticationRepository")
        final UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository) {
        return new DisplayUserGraphicsBeforeAuthenticationAction(userGraphicalAuthenticationRepository);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public Action initializeLoginAction(final CasConfigurationProperties casProperties,
                                        @Qualifier(ServicesManager.BEAN_NAME)
                                        final ServicesManager servicesManager) {
        return new PrepareForGraphicalAuthenticationAction(servicesManager, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer(
        @Qualifier("graphicalUserAuthenticationWebflowConfigurer")
        final CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(graphicalUserAuthenticationWebflowConfigurer);
    }
}
