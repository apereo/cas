package org.apereo.cas.config;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.impl.plans.RiskyAuthenticationException;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import org.apereo.cas.web.flow.CasFlowHandlerAdapter;
import org.apereo.cas.web.flow.CasFlowHandlerMapping;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.RiskAuthenticationCheckTokenAction;
import org.apereo.cas.web.flow.RiskAuthenticationVerificationWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionConfigurer;
import org.apereo.cas.web.flow.executor.WebflowExecutorFactory;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.executor.FlowExecutor;
import java.util.List;

/**
 * This is {@link ElectronicFenceWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Electrofence)
@AutoConfiguration
public class ElectronicFenceWebflowConfiguration {

    @Configuration(value = "RiskAuthenticationCoreConfiguration", proxyBeanMethods = false)
    public static class RiskAuthenticationCoreConfiguration {
        @ConditionalOnMissingBean(name = "riskAwareCasWebflowExceptionConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExceptionConfigurer riskAwareCasWebflowExceptionConfigurer() {
            return catalog -> catalog.registerException(RiskyAuthenticationException.class);
        }

        @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowEventResolver")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public CasWebflowEventResolver riskAwareAuthenticationWebflowEventResolver(
            @Qualifier("casWebflowConfigurationContext") final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
            @Qualifier("authenticationRiskMitigator") final AuthenticationRiskMitigator authenticationRiskMitigator,
            @Qualifier("authenticationRiskEvaluator") final AuthenticationRiskEvaluator authenticationRiskEvaluator,
            @Qualifier("initialAuthenticationAttemptWebflowEventResolver") final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
            val resolver = new RiskAwareAuthenticationWebflowEventResolver(casWebflowConfigurationContext,
                authenticationRiskEvaluator, authenticationRiskMitigator);
            initialAuthenticationAttemptWebflowEventResolver.addDelegate(resolver, 0);
            return resolver;
        }

        @ConditionalOnMissingBean(name = "riskAwareAuthenticationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer riskAwareAuthenticationWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY) final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
            return new RiskAwareAuthenticationWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskAwareCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer riskAwareCasWebflowExecutionPlanConfigurer(
            @Qualifier("riskAwareAuthenticationWebflowConfigurer") final CasWebflowConfigurer riskAwareAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(riskAwareAuthenticationWebflowConfigurer);
        }
    }

    @Configuration(value = "RiskAuthenticationVerificationConfiguration", proxyBeanMethods = false)
    public static class RiskAuthenticationVerificationConfiguration {
        private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
        public Action riskAuthenticationCheckTokenAction(
            @Qualifier(GeoLocationService.BEAN_NAME) final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier(CasEventRepository.BEAN_NAME) final CasEventRepository casEventRepository,
            @Qualifier("cookieCipherExecutor") final CipherExecutor cookieCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(CommunicationsManager.BEAN_NAME) final CommunicationsManager communicationsManager,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RiskAuthenticationCheckTokenAction(casEventRepository, communicationsManager, servicesManager,
                    cookieCipherExecutor, geoLocationService, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
                .build()
                .get();
        }


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public FlowExecutor riskVerificationFlowExecutor(
            final CasConfigurationProperties casProperties,
            @Qualifier("riskVerificationFlowRegistry") final FlowDefinitionRegistry riskVerificationFlowRegistry,
            @Qualifier("webflowCipherExecutor") final CipherExecutor webflowCipherExecutor) {
            val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                riskVerificationFlowRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS);
            return factory.build();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry riskVerificationFlowRegistry(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER) final FlowBuilder flowBuilder) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION);
            return builder.build();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public HandlerAdapter riskVerificationWebflowHandlerAdapter(
            @Qualifier("riskVerificationFlowExecutor") final FlowExecutor riskVerificationFlowExecutor) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION);
            handler.setFlowExecutor(riskVerificationFlowExecutor);
            handler.setFlowUrlHandler(new CasDefaultFlowUrlHandler());
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public HandlerMapping riskVerificationFlowHandlerMapping(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME) final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("riskVerificationFlowRegistry") final FlowDefinitionRegistry riskVerificationFlowRegistry) {
            val handler = new CasFlowHandlerMapping();
            handler.setOrder(0);
            handler.setFlowRegistry(riskVerificationFlowRegistry);
            handler.setInterceptors(webflowExecutionPlan.getWebflowInterceptors().toArray());
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskVerificationFlowEndpointConfigurer")
        public CasWebSecurityConfigurer<Void> riskVerificationFlowEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of(StringUtils.prependIfMissing(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION, "/"));
                }
            };
        }

        @ConditionalOnMissingBean(name = "riskVerificationWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer riskVerificationWebflowConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("riskVerificationFlowRegistry") final FlowDefinitionRegistry riskVerificationFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES) final FlowBuilderServices flowBuilderServices) {
            return new RiskAuthenticationVerificationWebflowConfigurer(flowBuilderServices,
                riskVerificationFlowRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskVerificationWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer riskVerificationWebflowExecutionPlanConfigurer(
            @Qualifier("riskVerificationWebflowConfigurer") final CasWebflowConfigurer riskVerificationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(riskVerificationWebflowConfigurer);
        }
    }
}
