package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailConstants;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.aup.DefaultAcceptableUsagePolicyRepository;
import org.apereo.cas.aup.GroovyAcceptableUsagePolicyRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.AcceptableUsagePolicySubmitAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyVerifyServiceAction;
import org.apereo.cas.web.flow.AcceptableUsagePolicyWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
 * This is {@link CasAcceptableUsagePolicyWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AcceptableUsagePolicy)
@AutoConfiguration
public class CasAcceptableUsagePolicyWebflowAutoConfiguration {

    @Configuration(value = "CasAcceptableUsagePolicyWebflowCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAcceptableUsagePolicyWebflowCoreConfiguration {

        @ConditionalOnMissingBean(name = "acceptableUsagePolicyWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer(
            final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> new AcceptableUsagePolicyWebflowConfigurer(flowBuilderServices,
                    flowDefinitionRegistry, applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasAcceptableUsagePolicyWebflowRepositoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAcceptableUsagePolicyWebflowRepositoryConfiguration {
        @ConditionalOnMissingBean(name = AcceptableUsagePolicyRepository.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(TicketRegistrySupport.BEAN_NAME)
            final TicketRegistrySupport ticketRegistrySupport) {
            return BeanSupplier.of(AcceptableUsagePolicyRepository.class)
                .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val groovy = casProperties.getAcceptableUsagePolicy().getGroovy();
                    if (groovy.getLocation() != null && CasRuntimeHintsRegistrar.notInNativeImage()) {
                        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
                        return new GroovyAcceptableUsagePolicyRepository(ticketRegistrySupport,
                            casProperties.getAcceptableUsagePolicy(),
                            scriptFactory.fromResource(groovy.getLocation()), applicationContext);
                    }
                    return new DefaultAcceptableUsagePolicyRepository(ticketRegistrySupport, casProperties.getAcceptableUsagePolicy());
                })
                .otherwise(AcceptableUsagePolicyRepository::noOp)
                .get();
        }
    }

    @Configuration(value = "CasAcceptableUsagePolicyWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAcceptableUsagePolicyWebflowPlanConfiguration {
        @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyWebflowExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer casAcceptableUsagePolicyWebflowExecutionPlanConfigurer(
            @Qualifier("acceptableUsagePolicyWebflowConfigurer")
            final CasWebflowConfigurer acceptableUsagePolicyWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(acceptableUsagePolicyWebflowConfigurer);
        }
    }

    @Configuration(value = "CasAcceptableUsagePolicyWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAcceptableUsagePolicyWebflowActionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUP_SUBMIT)
        public Action acceptableUsagePolicySubmitAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
            final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                    .supply(() -> new AcceptableUsagePolicySubmitAction(acceptableUsagePolicyRepository))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_AUP_SUBMIT)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        public Action acceptableUsagePolicyVerifyAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
            final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                    .supply(() -> new AcceptableUsagePolicyVerifyAction(acceptableUsagePolicyRepository, registeredServiceAccessStrategyEnforcer))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUP_RENDER)
        public Action acceptableUsagePolicyRenderAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
            final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                    .supply(() -> new ConsumerExecutionAction(requestContext -> acceptableUsagePolicyRepository.fetchPolicy(requestContext)
                        .ifPresent(policy -> WebUtils.putAcceptableUsagePolicyTermsIntoFlowScope(requestContext, policy))))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_AUP_RENDER)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUP_VERIFY_SERVICE)
        public Action acceptableUsagePolicyVerifyServiceAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AcceptableUsagePolicyRepository.BEAN_NAME)
            final AcceptableUsagePolicyRepository acceptableUsagePolicyRepository,
            @Qualifier(AuditableExecution.AUDITABLE_EXECUTION_REGISTERED_SERVICE_ACCESS)
            final AuditableExecution registeredServiceAccessStrategyEnforcer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                    .supply(() -> new AcceptableUsagePolicyVerifyServiceAction(acceptableUsagePolicyRepository, registeredServiceAccessStrategyEnforcer))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_AUP_VERIFY_SERVICE)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasAcceptableUsagePolicyWebflowAuditConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasAcceptableUsagePolicyWebflowAuditConfiguration {
        @ConditionalOnMissingBean(name = "casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuditTrailRecordResolutionPlanConfigurer casAcceptableUsagePolicyAuditTrailRecordResolutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("nullableReturnValueResourceResolver")
            final AuditResourceResolver resourceResolver) {

            return BeanSupplier.of(AuditTrailRecordResolutionPlanConfigurer.class)
                .when(AcceptableUsagePolicyRepository.CONDITION_AUP_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() ->
                    plan -> {
                        plan.registerAuditResourceResolver(resourceResolver,
                            AuditResourceResolvers.AUP_SUBMIT_RESOURCE_RESOLVER,
                            AuditResourceResolvers.AUP_VERIFY_RESOURCE_RESOLVER);
                        val resolver = new DefaultAuditActionResolver(AuditTrailConstants.AUDIT_ACTION_POSTFIX_TRIGGERED);
                        plan.registerAuditActionResolvers(resolver,
                            AuditActionResolvers.AUP_VERIFY_ACTION_RESOLVER,
                            AuditActionResolvers.AUP_SUBMIT_ACTION_RESOLVER);
                    })
                .otherwiseProxy()
                .get();
        }
    }
}
