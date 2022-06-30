package org.apereo.cas.config;

import org.apereo.cas.api.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.scim.v1.ScimV1PrincipalAttributeMapper;
import org.apereo.cas.scim.v1.ScimV1PrincipalProvisioner;
import org.apereo.cas.scim.v2.DefaultScimV2PrincipalAttributeMapper;
import org.apereo.cas.scim.v2.ScimV2PrincipalAttributeMapper;
import org.apereo.cas.scim.v2.ScimV2PrincipalProvisioner;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalScimProvisionerAction;
import org.apereo.cas.web.flow.ScimWebflowConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SCIM)
@AutoConfiguration
public class CasScimConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.scim.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasScimWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasScimWebflowConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SCIM_PROVISIONING_PRINCIPAL)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action principalScimProvisionerAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final PrincipalProvisioner scimProvisioner) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new PrincipalScimProvisionerAction(scimProvisioner))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "scimCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer scimCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("scimWebflowConfigurer")
            final CasWebflowConfigurer scimWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(scimWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "scimWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer scimWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new ScimWebflowConfigurer(flowBuilderServices,
                    loginFlowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasScimCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasScimCoreConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.scim.version").havingValue("2").evenIfMissing();

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "scim2PrincipalAttributeMapper")
        public ScimV2PrincipalAttributeMapper scim2PrincipalAttributeMapper(
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(ScimV2PrincipalAttributeMapper.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(DefaultScimV2PrincipalAttributeMapper::new)
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = PrincipalProvisioner.BEAN_NAME)
        public PrincipalProvisioner scimProvisioner(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("scim2PrincipalAttributeMapper")
            final ScimV2PrincipalAttributeMapper scim2PrincipalAttributeMapper) {
            return BeanSupplier.of(PrincipalProvisioner.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val scim = casProperties.getScim();
                    return new ScimV2PrincipalProvisioner(scim, scim2PrincipalAttributeMapper);
                })
                .otherwise(() -> {
                    val scim = casProperties.getScim();
                    return new ScimV1PrincipalProvisioner(scim, new ScimV1PrincipalAttributeMapper());
                })
                .get();
        }
    }
}
