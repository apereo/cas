package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.scim.v1.ScimV1PrincipalAttributeMapper;
import org.apereo.cas.scim.v1.ScimV1PrincipalProvisioner;
import org.apereo.cas.scim.v2.ScimV2PrincipalAttributeMapper;
import org.apereo.cas.scim.v2.ScimV2PrincipalProvisioner;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalScimProvisionerAction;
import org.apereo.cas.web.flow.ScimWebflowConfigurer;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasScimConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casScimConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnProperty(name = "cas.scim.target")
public class CasScimConfiguration {
    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "scimWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer scimWebflowConfigurer() {
        return new ScimWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "scim2PrincipalAttributeMapper")
    public ScimV2PrincipalAttributeMapper scim2PrincipalAttributeMapper() {
        return new ScimV2PrincipalAttributeMapper();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "scim1PrincipalAttributeMapper")
    public ScimV1PrincipalAttributeMapper scim1PrincipalAttributeMapper() {
        return new ScimV1PrincipalAttributeMapper();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "scimProvisioner")
    public PrincipalProvisioner scimProvisioner() {
        val scim = casProperties.getScim();
        if (casProperties.getScim().getVersion() == 1) {
            return new ScimV1PrincipalProvisioner(scim.getTarget(),
                scim.getOauthToken(),
                scim.getUsername(),
                scim.getPassword(),
                scim1PrincipalAttributeMapper());
        }
        return new ScimV2PrincipalProvisioner(scim.getTarget(),
            scim.getOauthToken(), scim.getUsername(), scim.getPassword(),
            scim2PrincipalAttributeMapper());
    }

    @ConditionalOnMissingBean(name = "principalScimProvisionerAction")
    @Bean
    @RefreshScope
    public Action principalScimProvisionerAction() {
        return new PrincipalScimProvisionerAction(scimProvisioner());
    }

    @Bean
    @ConditionalOnMissingBean(name = "scimCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer scimCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(scimWebflowConfigurer());
    }
}
