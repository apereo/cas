package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorWebflowConfigurer;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckAuthenticationAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboCheckUserAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboMustEnrollAction;
import org.apereo.cas.support.inwebo.web.flow.actions.InweboPushAuthenticateAction;
import org.apereo.cas.trusted.web.flow.BasicMultifactorTrustedWebflowConfigurer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.StaticEventExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * The Inwebo MFA webflow configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "inwebo")
@Configuration(value = "InweboWebflowConfiguration", proxyBeanMethods = false)
class InweboWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @ConditionalOnMissingBean(name = "inweboMultifactorWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer inweboMultifactorWebflowConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier("inweboFlowRegistry")
        final FlowDefinitionRegistry inweboFlowRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        val cfg = new InweboMultifactorWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry,
            inweboFlowRegistry,
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @ConditionalOnMissingBean(name = "inweboMultifactorAuthenticationWebflowEventResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowEventResolver inweboMultifactorAuthenticationWebflowEventResolver(
        @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return new FinalMultifactorAuthenticationTransactionWebflowEventResolver(casWebflowConfigurationContext);
    }

    @Configuration(value = "InweboWebflowRegistryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboWebflowRegistryConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public FlowDefinitionRegistry inweboFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, InweboMultifactorWebflowConfigurer.MFA_INWEBO_EVENT_ID);
            return builder.build();
        }

    }

    @Configuration(value = "InweboWebflowExecutionPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboWebflowExecutionPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "inweboCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer inweboCasWebflowExecutionPlanConfigurer(
            @Qualifier("inweboMultifactorWebflowConfigurer")
            final CasWebflowConfigurer inweboMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(inweboMultifactorWebflowConfigurer);
        }
    }

    @Configuration(value = "InweboWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class InweboWebflowActionConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INWEBO_PUSH_AUTHENTICATION)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action inweboPushAuthenticateAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboService")
            final InweboService inweboService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InweboPushAuthenticateAction(inweboService))
                .withId(CasWebflowConstants.ACTION_ID_INWEBO_PUSH_AUTHENTICATION)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INWEBO_CHECK_USER)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action inweboCheckUserAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("inweboService")
            final InweboService inweboService,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InweboCheckUserAction(inweboService, casProperties))
                .withId(CasWebflowConstants.ACTION_ID_INWEBO_CHECK_USER)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INWEBO_MUST_ENROLL)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action inweboMustEnrollAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(InweboMustEnrollAction::new)
                .withId(CasWebflowConstants.ACTION_ID_INWEBO_MUST_ENROLL)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INWEBO_CHECK_AUTHENTICATION)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action inweboCheckAuthenticationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("inweboMultifactorAuthenticationWebflowEventResolver")
            final CasWebflowEventResolver inweboMultifactorAuthenticationWebflowEventResolver,
            @Qualifier("inweboService")
            final InweboService inweboService) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new InweboCheckAuthenticationAction(inweboService,
                            inweboMultifactorAuthenticationWebflowEventResolver))
                .withId(CasWebflowConstants.ACTION_ID_INWEBO_CHECK_AUTHENTICATION)
                .build()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INWEBO_SUCCESS)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action inweboSuccessAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> StaticEventExecutionAction.SUCCESS)
                .withId(CasWebflowConstants.ACTION_ID_INWEBO_SUCCESS)
                .build()
                .get();
        }

    }

    /**
     * The Inwebo multifactor trust configuration.
     */
    @ConditionalOnClass(MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthenticationTrustedDevices, module = "inwebo")
    @Configuration(value = "InweboMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("inweboMultifactorWebflowConfigurer")
    static class InweboMultifactorTrustConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.inwebo.trusted-device-enabled")
            .isTrue().evenIfMissing();

        @ConditionalOnMissingBean(name = "inweboMultifactorTrustWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer inweboMultifactorTrustWebflowConfigurer(
            @Qualifier("inweboFlowRegistry")
            final FlowDefinitionRegistry inweboFlowRegistry,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val cfg = new BasicMultifactorTrustedWebflowConfigurer(flowBuilderServices,
                        flowDefinitionRegistry,
                        inweboFlowRegistry,
                        applicationContext,
                        casProperties,
                        MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
                    cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
                    return cfg;
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer inweboMultifactorTrustCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("inweboMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer inweboMultifactorTrustWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(inweboMultifactorTrustWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
