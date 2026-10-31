package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorAccountProfileContactMethodsAction;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorAccountProfileWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
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
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSimpleMultifactorAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "CasSimpleMultifactorAuthenticationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationBaseConfiguration {
        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer(
            @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val cfg = new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, mfaSimpleAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaSimpleAuthenticatorFlowRegistry")
        public FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, casProperties.getAuthn().getMfa().getSimple().getId());
            return builder.build();
        }

        @ConditionalOnMissingBean(name = CasSimpleMultifactorTokenCommunicationStrategy.BEAN_NAME)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
            return CasSimpleMultifactorTokenCommunicationStrategy.all();
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationAccountProfileConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.AccountManagement, enabledByDefault = false)
    static class CasSimpleMultifactorAuthenticationAccountProfileConfiguration {
        @ConditionalOnMissingBean(name = "mfaSimpleAccountProfileWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaSimpleAccountProfileWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return new CasSimpleMultifactorAccountProfileWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaSimpleAccountProfileWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer mfaSimpleAccountProfileWebflowExecutionPlanConfigurer(
            @Qualifier("mfaSimpleAccountProfileWebflowConfigurer")
            final CasWebflowConfigurer mfaSimpleAccountProfileWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleAccountProfileWebflowConfigurer);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_CONTACT_METHODS)
        public Action simpleMfaAccountProfileContactMethodsAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CasSimpleMultifactorAccountProfileContactMethodsAction(casProperties.getAuthn().getMfa().getSimple()))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_CONTACT_METHODS)
                .build()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_REGISTRATION)
        public Action simpleMfaAccountProfileRegistrationAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("casSimpleMultifactorAuthenticationProvider")
            final MultifactorAuthenticationProvider casSimpleMultifactorAuthenticationProvider) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new ConsumerExecutionAction(requestContext -> {
                    MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, casSimpleMultifactorAuthenticationProvider);
                    requestContext.getFlowScope().put("allowRegisterEmail", Boolean.TRUE);
                }))
                .withId(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_SIMPLE_MFA_REGISTRATION)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasSimpleMultifactorSurrogateAuthenticationWebflowPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(SurrogateAuthenticationService.class)
    static class CasSimpleMultifactorSurrogateAuthenticationWebflowPlanConfiguration {
        @Bean
        @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SurrogateAuthentication)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "surrogateCasSimpleMultifactorAuthenticationWebflowConfigurer")
        public CasWebflowConfigurer surrogateCasSimpleMultifactorAuthenticationWebflowConfigurer(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, applicationContext, casProperties);
        }

        @Bean
        @ConditionalOnMissingBean(name = "surrogateCasSimpleMultifactorAuthenticationWebflowExecutionPlanConfigurer")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowExecutionPlanConfigurer surrogateCasSimpleMultifactorAuthenticationWebflowExecutionPlanConfigurer(
            @Qualifier("surrogateCasSimpleMultifactorAuthenticationWebflowConfigurer")
            final CasWebflowConfigurer surrogateWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(surrogateWebflowConfigurer);
        }

        private static final class SurrogateWebflowConfigurer extends AbstractCasWebflowConfigurer {
            SurrogateWebflowConfigurer(
                final FlowBuilderServices flowBuilderServices,
                final FlowDefinitionRegistry mainFlowDefinitionRegistry,
                final ConfigurableApplicationContext applicationContext,
                final CasConfigurationProperties casProperties) {
                super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
                setOrder(Ordered.LOWEST_PRECEDENCE);
            }

            @Override
            protected void doInitialize() {
                val mfaState = getState(getLoginFlow(), casProperties.getAuthn().getMfa().getSimple().getId());
                createTransitionForState(mfaState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION, true);
            }
        }
    }
}
