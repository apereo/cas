package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.bucket4j.producer.BucketStore;
import org.apereo.cas.bucket4j.producer.InMemoryBucketStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationService;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasTwilioMultifactorSendTokenAction;
import org.apereo.cas.web.flow.CasTwilioMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasTwilioMultifactorAuthenticationWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "twilio")
@Configuration(value = "CasTwilioMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
class CasTwilioMultifactorAuthenticationWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION_BUCKET4J_ENABLED = BeanCondition.on("cas.authn.mfa.twilio.bucket4j.enabled").isTrue();

    @Configuration(value = "CasTwilioMultifactorAuthenticationActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasTwilioMultifactorAuthenticationActionConfiguration {
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_TWILIO_SEND_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaTwilioMultifactorSendTokenAction(
            @Qualifier(CasTwilioMultifactorAuthenticationService.BEAN_NAME)
            final CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("mfaTwilioMultifactorBucketConsumer")
            final BucketConsumer mfaTwilioMultifactorBucketConsumer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    val twilio = casProperties.getAuthn().getMfa().getTwilio();
                    return new CasTwilioMultifactorSendTokenAction(casTwilioMultifactorAuthenticationService,
                        twilio, mfaTwilioMultifactorBucketConsumer, tenantExtractor);
                })
                .withId(CasWebflowConstants.ACTION_ID_MFA_TWILIO_SEND_TOKEN)
                .build()
                .get();
        }


        @ConditionalOnMissingBean(name = "mfaTwilioMultifactorBucketConsumer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BucketConsumer mfaTwilioMultifactorBucketConsumer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaTwilioMultifactorBucketStore")
            final BucketStore mfaTwilioMultifactorBucketStore,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BucketConsumer.class)
                .when(CONDITION_BUCKET4J_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val twilio = casProperties.getAuthn().getMfa().getTwilio();
                    return new DefaultBucketConsumer(mfaTwilioMultifactorBucketStore, twilio.getBucket4j());
                })
                .otherwise(BucketConsumer::permitAll)
                .get();
        }

        @ConditionalOnMissingBean(name = "mfaTwilioMultifactorBucketStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BucketStore mfaTwilioMultifactorBucketStore(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BucketStore.class)
                .when(CONDITION_BUCKET4J_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val twilio = casProperties.getAuthn().getMfa().getTwilio();
                    return new InMemoryBucketStore(twilio.getBucket4j());
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasTwilioMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasTwilioMultifactorAuthenticationPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaTwilioCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer mfaTwilioCasWebflowExecutionPlanConfigurer(
            @Qualifier("mfaTwilioMultifactorWebflowConfigurer")
            final CasWebflowConfigurer mfaTwilioMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaTwilioMultifactorWebflowConfigurer);
        }
    }
    
    @Configuration(value = "CasTwilioMultifactorAuthenticationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasTwilioMultifactorAuthenticationBaseConfiguration {
        @ConditionalOnMissingBean(name = "mfaTwilioMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaTwilioMultifactorWebflowConfigurer(
            @Qualifier("mfaTwilioAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaTwilioAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry flowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val cfg = new CasTwilioMultifactorWebflowConfigurer(flowBuilderServices,
                flowDefinitionRegistry, mfaTwilioAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaTwilioAuthenticatorFlowRegistry")
        public FlowDefinitionRegistry mfaTwilioAuthenticatorFlowRegistry(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, casProperties.getAuthn().getMfa().getTwilio().getId());
            return builder.build();
        }
    }
}
