package org.apereo.cas.config;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.impl.plans.RiskyAuthenticationException;
import org.apereo.cas.multitenancy.TenantExtractor;
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
import org.apereo.cas.web.flow.CasWebflowIdExtractor;
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
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
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
@Configuration(value = "ElectronicFenceWebflowConfiguration", proxyBeanMethods = false)
class ElectronicFenceWebflowConfiguration {

    @Configuration(value = "RiskAuthenticationCoreConfiguration", proxyBeanMethods = false)
    static class RiskAuthenticationCoreConfiguration {
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
            @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
            final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext,
            @Qualifier("authenticationRiskMitigator")
            final AuthenticationRiskMitigator authenticationRiskMitigator,
            @Qualifier("authenticationRiskEvaluator")
            final AuthenticationRiskEvaluator authenticationRiskEvaluator,
            @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
            final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
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
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new RiskAwareAuthenticationWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskAwareCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer riskAwareCasWebflowExecutionPlanConfigurer(
            @Qualifier("riskAwareAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer riskAwareAuthenticationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(riskAwareAuthenticationWebflowConfigurer);
        }
    }

    @Configuration(value = "RiskAuthenticationVerificationConfiguration", proxyBeanMethods = false)
    static class RiskAuthenticationVerificationConfiguration {
        private static final FlowExecutionListener[] FLOW_EXECUTION_LISTENERS = new FlowExecutionListener[0];

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
        public Action riskAuthenticationCheckTokenAction(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver principalResolver,
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            @Qualifier(CasEventRepository.BEAN_NAME)
            final CasEventRepository casEventRepository,
            @Qualifier("cookieCipherExecutor")
            final CipherExecutor cookieCipherExecutor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new RiskAuthenticationCheckTokenAction(
                    casEventRepository, communicationsManager, servicesManager,
                    principalResolver, cookieCipherExecutor, geoLocationService,
                    casProperties, webApplicationServiceFactory, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_RISK_AUTHENTICATION_TOKEN_CHECK)
                .build()
                .get();
        }


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "riskVerificationFlowExecutor")
        public FlowExecutor riskVerificationFlowExecutor(
            @Qualifier("riskVerificationWebflowUrlHandler")
            final FlowUrlHandler riskVerificationWebflowUrlHandler,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier("webflowCipherExecutor")
            final CipherExecutor webflowCipherExecutor) {
            val factory = new WebflowExecutorFactory(casProperties.getWebflow(),
                flowDefinitionRegistry, webflowCipherExecutor, FLOW_EXECUTION_LISTENERS,
                riskVerificationWebflowUrlHandler);
            return factory.build();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "riskVerificationWebflowUrlHandler")
        public FlowUrlHandler riskVerificationWebflowUrlHandler(final List<CasWebflowIdExtractor> flowIdExtractors) {
            return new CasDefaultFlowUrlHandler(flowIdExtractors);
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "riskVerificationWebflowHandlerAdapter")
        public HandlerAdapter riskVerificationWebflowHandlerAdapter(
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
            final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier("riskVerificationWebflowUrlHandler")
            final FlowUrlHandler riskVerificationWebflowUrlHandler,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("riskVerificationFlowExecutor")
            final FlowExecutor riskVerificationFlowExecutor) {
            val handler = new CasFlowHandlerAdapter(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION, webflowExecutionPlan);
            handler.setFlowExecutor(riskVerificationFlowExecutor);
            handler.setFlowUrlHandler(riskVerificationWebflowUrlHandler);
            return handler;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskVerificationFlowHandlerMapping")
        public HandlerMapping riskVerificationFlowHandlerMapping(
            @Qualifier("riskVerificationWebflowUrlHandler")
            final FlowUrlHandler riskVerificationWebflowUrlHandler,
            @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
            final CasWebflowExecutionPlan webflowExecutionPlan,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry) {
            val handler = new CasFlowHandlerMapping();
            handler.setOrder(0);
            handler.setFlowUrlHandler(riskVerificationWebflowUrlHandler);
            handler.setFlowRegistry(flowDefinitionRegistry);
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
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new RiskAuthenticationVerificationWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "riskVerificationWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer riskVerificationWebflowExecutionPlanConfigurer(
            @Qualifier("riskVerificationWebflowConfigurer")
            final CasWebflowConfigurer riskVerificationWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(riskVerificationWebflowConfigurer);
        }
    }
}
