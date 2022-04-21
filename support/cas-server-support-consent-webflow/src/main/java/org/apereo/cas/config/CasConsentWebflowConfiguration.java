package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.consent.ConsentActivationStrategy;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.CheckConsentRequiredAction;
import org.apereo.cas.web.flow.ConfirmConsentAction;
import org.apereo.cas.web.flow.ConsentWebflowConfigurer;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 * This is {@link CasConsentWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnBean(name = ConsentRepository.BEAN_NAME)
@Configuration(value = "CasConsentWebflowConfiguration", proxyBeanMethods = false)
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.Consent)
public class CasConsentWebflowConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.consent.core.enabled").isTrue().evenIfMissing();

    @Configuration(value = "CasConsentWebflowActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentWebflowActionConfiguration {

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CHECK_CONSENT_REQUIRED)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action checkConsentRequiredAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier(ConsentEngine.BEAN_NAME)
            final ConsentEngine consentEngine,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(ConsentActivationStrategy.BEAN_NAME)
            final ConsentActivationStrategy consentActivationStrategy) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new CheckConsentRequiredAction(servicesManager,
                        authenticationRequestServiceSelectionStrategies,
                        consentEngine, casProperties, attributeDefinitionStore,
                        applicationContext, consentActivationStrategy))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_CHECK_CONSENT_REQUIRED)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CONFIRM_CONSENT)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action confirmConsentAction(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
            @Qualifier(ConsentEngine.BEAN_NAME)
            final ConsentEngine consentEngine,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> BeanSupplier.of(Action.class)
                    .when(CONDITION.given(applicationContext.getEnvironment()))
                    .supply(() -> new ConfirmConsentAction(servicesManager, authenticationRequestServiceSelectionStrategies,
                        consentEngine, casProperties, attributeDefinitionStore, applicationContext))
                    .otherwise(() -> ConsumerExecutionAction.NONE)
                    .get())
                .withId(CasWebflowConstants.ACTION_ID_CONFIRM_CONSENT)
                .build()
                .get();
        }
    }

    @Configuration(value = "CasConsentWebflowBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasConsentWebflowBaseConfiguration {

        @ConditionalOnMissingBean(name = "consentWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer consentWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
            return BeanSupplier.of(CasWebflowConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new ConsentWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                    applicationContext, casProperties))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "consentCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer consentCasWebflowExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("consentWebflowConfigurer")
            final CasWebflowConfigurer consentWebflowConfigurer) {
            return BeanSupplier.of(CasWebflowExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerWebflowConfigurer(consentWebflowConfigurer))
                .otherwiseProxy()
                .get();
        }
    }
}
