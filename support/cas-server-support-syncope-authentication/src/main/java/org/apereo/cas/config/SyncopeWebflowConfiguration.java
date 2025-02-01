package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.syncope.SyncopePrincipalProvisioner;
import org.apereo.cas.syncope.web.flow.SyncopeWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalProvisionerAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SyncopeWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "SyncopeWebflowConfiguration", proxyBeanMethods = false)
class SyncopeWebflowConfiguration {

    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Provisioning, module = "syncope")
    @Configuration(value = "SyncopeProvisioningWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class SyncopeProvisioningWebflowConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.syncope.provisioning.enabled").isTrue();

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalProvisioner principalProvisioner(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(PrincipalProvisioner.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SyncopePrincipalProvisioner(casProperties.getAuthn().getSyncope().getProvisioning()))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SYNCOPE_PRINCIPAL_PROVISIONER_ACTION)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action syncopePrincipalProvisionerAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalProvisioner.BEAN_NAME)
            final PrincipalProvisioner principalProvisioner) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new PrincipalProvisionerAction(principalProvisioner, casProperties.getScim()))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "syncopeCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer syncopeCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("syncopeWebflowConfigurer")
            final CasWebflowConfigurer syncopeWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(syncopeWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "syncopeWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer syncopeWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new SyncopeWebflowConfigurer(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }
    }
}
