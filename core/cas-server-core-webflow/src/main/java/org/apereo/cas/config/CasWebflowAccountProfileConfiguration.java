package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasFlowHandlerMapping;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CasWebflowIdExtractor;
import org.apereo.cas.web.flow.configurer.acct.AccountProfileWebflowConfigurer;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import java.util.List;

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
@Configuration(value = "CasWebflowAccountProfileConfiguration", proxyBeanMethods = false)
class CasWebflowAccountProfileConfiguration {
    private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

    @ConditionalOnMissingBean(name = "accountProfileWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer accountProfileWebflowConfigurer(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new AccountProfileWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountProfileWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer accountProfileWebflowExecutionPlanConfigurer(
        @Qualifier("accountProfileWebflowConfigurer")
        final CasWebflowConfigurer accountProfileWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(accountProfileWebflowConfigurer);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public FlowExecutor accountProfileFlowExecutor(
        @Qualifier(TenantExtractor.BEAN_NAME)
        final TenantExtractor tenantExtractor,
        @Qualifier("accountProfileWebflowUrlHandler")
        final FlowUrlHandler accountProfileWebflowUrlHandler,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
        final CipherExecutor webflowCipherExecutor) {
        val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
            flowDefinitionRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS,
            accountProfileWebflowUrlHandler, tenantExtractor);
        return factory.build();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public FlowUrlHandler accountProfileWebflowUrlHandler(
        final List<CasWebflowIdExtractor> flowIdExtractors) {
        return new CasDefaultFlowUrlHandler(flowIdExtractors);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public HandlerAdapter accountProfileWebflowHandlerAdapter(
        @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
        final CasWebflowExecutionPlan webflowExecutionPlan,
        @Qualifier("accountProfileWebflowUrlHandler")
        final FlowUrlHandler accountProfileWebflowUrlHandler,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("accountProfileFlowExecutor")
        final FlowExecutor accountProfileFlowExecutor) {
        val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_ACCOUNT, webflowExecutionPlan);
        handler.setFlowExecutor(accountProfileFlowExecutor);
        handler.setFlowUrlHandler(accountProfileWebflowUrlHandler);
        return handler;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public HandlerMapping accountProfileFlowHandlerMapping(
        @Qualifier("accountProfileWebflowUrlHandler")
        final FlowUrlHandler accountProfileWebflowUrlHandler,
        @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
        final CasWebflowExecutionPlan webflowExecutionPlan,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry) {
        val handler = new CasFlowHandlerMapping();
        handler.setOrder(0);
        handler.setFlowRegistry(flowDefinitionRegistry);
        handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
        handler.setFlowUrlHandler(accountProfileWebflowUrlHandler);
        return handler;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "accountProfileFlowEndpointConfigurer")
    public CasWebSecurityConfigurer<Void> accountProfileFlowEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(StringUtils.prependIfMissing(CasWebflowConfigurer.FLOW_ID_ACCOUNT, "/"));
            }
        };
    }
}
