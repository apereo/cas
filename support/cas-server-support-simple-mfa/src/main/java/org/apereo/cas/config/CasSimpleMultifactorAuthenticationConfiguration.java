package org.apereo.cas.config;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.bucket4j.consumer.DefaultBucketConsumer;
import org.apereo.cas.bucket4j.producer.BucketStore;
import org.apereo.cas.bucket4j.producer.InMemoryBucketStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketImpl;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator;
import org.apereo.cas.mfa.simple.ticket.DefaultCasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.mfa.simple.web.CasSimpleMultifactorAuthenticationEndpoint;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorSendTokenAction;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
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
import java.io.Serial;

/**
 * This is {@link CasSimpleMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    private static final BeanCondition CONDITION_BUCKET4J_ENABLED = BeanCondition.on("cas.authn.mfa.simple.bucket4j.enabled").isTrue();

    @Configuration(value = "CasSimpleMultifactorAuthenticationActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationActionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public CasSimpleMultifactorAuthenticationEndpoint mfaSimpleMultifactorEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new CasSimpleMultifactorAuthenticationEndpoint(casProperties, applicationContext);
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaSimpleMultifactorSendTokenAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
            final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
            @Qualifier("mfaSimpleMultifactorTokenCommunicationStrategy")
            final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
            final CasConfigurationProperties casProperties,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier("mfaSimpleMultifactorBucketConsumer")
            final BucketConsumer mfaSimpleMultifactorBucketConsumer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    val simple = casProperties.getAuthn().getMfa().getSimple();
                    return new CasSimpleMultifactorSendTokenAction(
                        communicationsManager, casSimpleMultifactorAuthenticationService, simple,
                        mfaSimpleMultifactorTokenCommunicationStrategy,
                        mfaSimpleMultifactorBucketConsumer);
                })
                .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorBucketConsumer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BucketConsumer mfaSimpleMultifactorBucketConsumer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("mfaSimpleMultifactorBucketStore")
            final BucketStore mfaSimpleMultifactorBucketStore,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BucketConsumer.class)
                .when(CONDITION_BUCKET4J_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val simple = casProperties.getAuthn().getMfa().getSimple();
                    return new DefaultBucketConsumer(mfaSimpleMultifactorBucketStore, simple.getBucket4j());
                })
                .otherwise(BucketConsumer::permitAll)
                .get();
        }

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorBucketStore")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BucketStore mfaSimpleMultifactorBucketStore(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(BucketStore.class)
                .when(CONDITION_BUCKET4J_ENABLED.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val simple = casProperties.getAuthn().getMfa().getSimple();
                    return new InMemoryBucketStore(simple.getBucket4j());
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "mfaSimpleCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer mfaSimpleCasWebflowExecutionPlanConfigurer(
            @Qualifier("mfaSimpleMultifactorWebflowConfigurer")
            final CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorWebflowConfigurer);
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationBaseConfiguration {
        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer(
            @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val cfg = new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices,
                loginFlowRegistry,
                mfaSimpleAuthenticatorFlowRegistry, applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
            return cfg;
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationWebflowConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationWebflowConfiguration {
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

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTokenCommunicationStrategy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
            return CasSimpleMultifactorTokenCommunicationStrategy.all();
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationTicketConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator(final CasConfigurationProperties casProperties) {
            val simple = casProperties.getAuthn().getMfa().getSimple().getToken().getCore();
            return new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(simple.getTokenLength());
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationTicketFactoryConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory(
            @Qualifier("casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
            final UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator,
            @Qualifier("casSimpleMultifactorAuthenticationTicketExpirationPolicy")
            final ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy) {
            return new DefaultCasSimpleMultifactorAuthenticationTicketFactory(
                casSimpleMultifactorAuthenticationTicketExpirationPolicy,
                casSimpleMultifactorAuthenticationUniqueTicketIdGenerator);
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketSerializationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationTicketSerializationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketSerializationExecutionPlanConfigurer casSimpleMultifactorAuthenticationTicketSerializationExecutionPlanConfigurer() {
            return plan -> {
                plan.registerTicketSerializer(new CasSimpleMultifactorAuthenticationTicketStringSerializer());
                plan.registerTicketSerializer(CasSimpleMultifactorAuthenticationTicket.class.getName(),
                    new CasSimpleMultifactorAuthenticationTicketStringSerializer());
            };
        }

        private static final class CasSimpleMultifactorAuthenticationTicketStringSerializer
            extends AbstractJacksonBackedStringSerializer<CasSimpleMultifactorAuthenticationTicketImpl> {
            @Serial
            private static final long serialVersionUID = -2198623586274810263L;

            CasSimpleMultifactorAuthenticationTicketStringSerializer() {
                super(MINIMAL_PRETTY_PRINTER);
            }

            @Override
            public Class<CasSimpleMultifactorAuthenticationTicketImpl> getTypeToSerialize() {
                return CasSimpleMultifactorAuthenticationTicketImpl.class;
            }
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasSimpleMultifactorAuthenticationTicketFactoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public TicketFactoryExecutionPlanConfigurer casSimpleMultifactorAuthenticationTicketFactoryConfigurer(
            @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
            final CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory) {
            return () -> casSimpleMultifactorAuthenticationTicketFactory;
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
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new SurrogateWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry, applicationContext, casProperties);
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
