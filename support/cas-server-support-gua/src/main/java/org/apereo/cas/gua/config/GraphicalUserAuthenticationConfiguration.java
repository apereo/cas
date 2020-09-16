package org.apereo.cas.gua.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.LdapUserGraphicalAuthenticationRepository;
import org.apereo.cas.gua.impl.StaticUserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.flow.AcceptUserGraphicsForAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayUserGraphicsBeforeAuthenticationAction;
import org.apereo.cas.web.flow.GraphicalUserAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForGraphicalAuthenticationAction;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link GraphicalUserAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("graphicalUserAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GraphicalUserAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer graphicalUserAuthenticationWebflowConfigurer() {
        return new GraphicalUserAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "userGraphicalAuthenticationRepository")
    public UserGraphicalAuthenticationRepository userGraphicalAuthenticationRepository() {
        val gua = casProperties.getAuthn().getGua();
        if (gua.getResource().getLocation() != null) {
            return new StaticUserGraphicalAuthenticationRepository(gua.getResource().getLocation());
        }

        val ldap = gua.getLdap();
        if (StringUtils.isNotBlank(ldap.getLdapUrl())
            && StringUtils.isNotBlank(ldap.getSearchFilter())
            && StringUtils.isNotBlank(ldap.getBaseDn())
            && StringUtils.isNotBlank(ldap.getImageAttribute())) {
            val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(gua.getLdap());
            return new LdapUserGraphicalAuthenticationRepository(casProperties, connectionFactory);
        }
        throw new BeanCreationException("A repository instance must be configured to locate user-defined graphics");
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptUserGraphicsForAuthenticationAction")
    @RefreshScope
    public Action acceptUserGraphicsForAuthenticationAction() {
        return new AcceptUserGraphicsForAuthenticationAction();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "displayUserGraphicsBeforeAuthenticationAction")
    public Action displayUserGraphicsBeforeAuthenticationAction() {
        return new DisplayUserGraphicsBeforeAuthenticationAction(userGraphicalAuthenticationRepository());
    }

    @Bean
    @RefreshScope
    public Action initializeLoginAction() {
        return new PrepareForGraphicalAuthenticationAction(servicesManager.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer graphicalUserAuthenticationCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(graphicalUserAuthenticationWebflowConfigurer());
    }
}
