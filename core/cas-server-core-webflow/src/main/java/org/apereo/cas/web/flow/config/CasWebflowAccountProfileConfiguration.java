package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasFlowHandlerMapping;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.configurer.acct.AccountProfileWebflowConfigurer;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;

import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowAccountProfileConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Webflow),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
})
@AutoConfiguration
public class CasWebflowAccountProfileConfiguration {
    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @ConditionalOnMissingBean(name = "accountProfileWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer accountProfileWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry accountProfileFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
        return new AccountProfileWebflowConfigurer(flowBuilderServices,
            accountProfileFlowRegistry, loginFlowRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountProfileWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer accountProfileWebflowExecutionPlanConfigurer(
        @Qualifier("accountProfileWebflowConfigurer") final CasWebflowConfigurer accountProfileWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(accountProfileWebflowConfigurer);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public FlowExecutor accountProfileFlowExecutor(
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry accountProfileFlowRegistry,
        @Qualifier("webflowCipherExecutor") final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            accountProfileFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
        return factory.build();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public FlowDefinitionRegistry accountProfileFlowRegistry(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) {
        val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
        builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        return builder.build();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public HandlerAdapter accountProfileWebflowHandlerAdapter(
        @Qualifier("accountProfileFlowExecutor") final FlowExecutor accountProfileFlowExecutor) {
        val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        handler.setFlowExecutor(accountProfileFlowExecutor);
        handler.setFlowUrlHandler(new CasDefaultFlowUrlHandler());
        return handler;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HandlerMapping accountProfileFlowHandlerMapping(
        @Qualifier(CasWebflowExecutionPlan.BEAN_NAME) final CasWebflowExecutionPlan webflowExecutionPlan,
        @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry accountProfileFlowRegistry) {
        val handler = new CasFlowHandlerMapping();
        handler.setOrder(0);
        handler.setFlowRegistry(accountProfileFlowRegistry);
        handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
        return handler;
    }
}
