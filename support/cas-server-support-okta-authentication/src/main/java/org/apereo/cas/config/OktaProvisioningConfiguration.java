package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalProvisioner;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.okta.OktaConfigurationFactory;
import org.apereo.cas.okta.OktaPrincipalProvisioner;
import org.apereo.cas.okta.web.flow.OktaWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.PrincipalProvisionerAction;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import com.okta.sdk.client.Client;
import lombok.val;
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
 * This is {@link OktaProvisioningConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Provisioning, module = "okta")
@Configuration(value = "OktaProvisioningConfiguration", proxyBeanMethods = false)
class OktaProvisioningConfiguration {

    private static final BeanCondition CONDITION = BeanCondition
        .on("cas.authn.okta.provisioning.enabled").isTrue()
        .and("cas.authn.okta.provisioning.organization-url");

    @ConditionalOnMissingBean(name = "oktaProvisioningClient")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Client oktaProvisioningClient(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(Client.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val properties = casProperties.getAuthn().getOkta().getProvisioning();
                return OktaConfigurationFactory.buildClient(properties);
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalProvisioner principalProvisioner(
        @Qualifier("oktaProvisioningClient")
        final Client oktaProvisioningClient,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(PrincipalProvisioner.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new OktaPrincipalProvisioner(oktaProvisioningClient, casProperties.getAuthn().getOkta().getProvisioning()))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_OKTA_PRINCIPAL_PROVISIONER_ACTION)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action oktaPrincipalProvisionerAction(
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
    @ConditionalOnMissingBean(name = "oktaCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer oktaCasWebflowExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("oktaWebflowConfigurer")
        final CasWebflowConfigurer oktaWebflowConfigurer) {
        return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerWebflowConfigurer(oktaWebflowConfigurer))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "oktaWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer oktaWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return BeanSupplier.of(CasWebflowConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new OktaWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties))
            .otherwiseProxy()
            .get();
    }

}
