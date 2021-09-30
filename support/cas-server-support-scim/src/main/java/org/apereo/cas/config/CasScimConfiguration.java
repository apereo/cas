package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.scim.v1.ScimV1PrincipalAttributeMapper;
import org.apereo.cas.scim.v1.ScimV1PrincipalProvisioner;
import org.apereo.cas.scim.v2.ScimV2PrincipalAttributeMapper;
import org.apereo.cas.scim.v2.ScimV2PrincipalProvisioner;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalScimProvisionerAction;
import org.apereo.cas.web.flow.ScimWebflowConfigurer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnProperty(prefix = "cas.scim", name = "enabled", havingValue = "true", matchIfMissing = true)
@Configuration(value = "casScimConfiguration", proxyBeanMethods = false)
public class CasScimConfiguration {

    @ConditionalOnMissingBean(name = "scimWebflowConfigurer")
    @Bean
    @Autowired
    public CasWebflowConfigurer scimWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new ScimWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "scim2PrincipalAttributeMapper")
    public ScimV2PrincipalAttributeMapper scim2PrincipalAttributeMapper() {
        return new ScimV2PrincipalAttributeMapper();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "scim1PrincipalAttributeMapper")
    public ScimV1PrincipalAttributeMapper scim1PrincipalAttributeMapper() {
        return new ScimV1PrincipalAttributeMapper();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "scimProvisioner")
    @Autowired
    public PrincipalProvisioner scimProvisioner(final CasConfigurationProperties casProperties,
                                                @Qualifier("scim1PrincipalAttributeMapper")
                                                final ScimV1PrincipalAttributeMapper scim1PrincipalAttributeMapper,
                                                @Qualifier("scim2PrincipalAttributeMapper")
                                                final ScimV2PrincipalAttributeMapper scim2PrincipalAttributeMapper) {
        val scim = casProperties.getScim();
        if (scim.getVersion() == 1) {
            return new ScimV1PrincipalProvisioner(scim, scim1PrincipalAttributeMapper);
        }
        return new ScimV2PrincipalProvisioner(scim, scim2PrincipalAttributeMapper);
    }

    @ConditionalOnMissingBean(name = "principalScimProvisionerAction")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action principalScimProvisionerAction(
        @Qualifier("scimProvisioner")
        final PrincipalProvisioner scimProvisioner) {
        return new PrincipalScimProvisionerAction(scimProvisioner);
    }

    @Bean
    @ConditionalOnMissingBean(name = "scimCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer scimCasWebflowExecutionPlanConfigurer(
        @Qualifier("scimWebflowConfigurer")
        final CasWebflowConfigurer scimWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(scimWebflowConfigurer);
    }
}
