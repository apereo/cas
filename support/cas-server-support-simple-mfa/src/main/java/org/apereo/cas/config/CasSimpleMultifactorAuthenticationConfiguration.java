package org.apereo.cas.config;

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
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorUpdateEmailAction;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorVerifyEmailAction;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.EnableScheduling;
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
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
            final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
            @Qualifier(CasSimpleMultifactorTokenCommunicationStrategy.BEAN_NAME)
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
                        mfaSimpleMultifactorBucketConsumer, tenantExtractor);
                })
                .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_SEND_TOKEN)
                .build()
                .get();
        }
        
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SIMPLE_UPDATE_EMAIL)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaSimpleMultifactorUpdateEmailAction(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
            final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
            @Qualifier(CasSimpleMultifactorTokenCommunicationStrategy.BEAN_NAME)
            final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
            final CasConfigurationProperties casProperties,
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier("mfaSimpleMultifactorBucketConsumer")
            final BucketConsumer mfaSimpleMultifactorBucketConsumer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> {
                    val simple = casProperties.getAuthn().getMfa().getSimple();
                    return new CasSimpleMultifactorUpdateEmailAction(
                        communicationsManager, casSimpleMultifactorAuthenticationService,
                        simple,
                        mfaSimpleMultifactorTokenCommunicationStrategy,
                        mfaSimpleMultifactorBucketConsumer, tenantExtractor);
                })
                .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_UPDATE_EMAIL)
                .build()
                .get();
        }

        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VERIFY_EMAIL)
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Action mfaSimpleMultifactorVerifyEmailAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
            final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService,
            @Qualifier(CasSimpleMultifactorTokenCommunicationStrategy.BEAN_NAME)
            final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
            final CasConfigurationProperties casProperties,
            @Qualifier(CommunicationsManager.BEAN_NAME)
            final CommunicationsManager communicationsManager,
            @Qualifier("mfaSimpleMultifactorBucketConsumer")
            final BucketConsumer mfaSimpleMultifactorBucketConsumer) {
            return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CasSimpleMultifactorVerifyEmailAction(
                    communicationsManager, casSimpleMultifactorAuthenticationService,
                    casProperties.getAuthn().getMfa().getSimple(),
                    mfaSimpleMultifactorTokenCommunicationStrategy,
                    mfaSimpleMultifactorBucketConsumer, tenantExtractor))
                .withId(CasWebflowConstants.ACTION_ID_MFA_SIMPLE_VERIFY_EMAIL)
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
        public UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator(
            final CasConfigurationProperties casProperties) {
            return new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(casProperties);
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
        public TicketSerializationExecutionPlanConfigurer casSimpleMultifactorAuthenticationTicketSerializationExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext) {
            return plan -> {
                plan.registerTicketSerializer(new CasSimpleMultifactorAuthenticationTicketStringSerializer(applicationContext));
                plan.registerTicketSerializer(CasSimpleMultifactorAuthenticationTicket.class.getName(),
                    new CasSimpleMultifactorAuthenticationTicketStringSerializer(applicationContext));
                plan.registerTicketSerializer(CasSimpleMultifactorAuthenticationTicket.PREFIX,
                    new CasSimpleMultifactorAuthenticationTicketStringSerializer(applicationContext));
            };
        }

        private static final class CasSimpleMultifactorAuthenticationTicketStringSerializer
            extends BaseJacksonSerializer<CasSimpleMultifactorAuthenticationTicketImpl> {
            @Serial
            private static final long serialVersionUID = -2198623586274810263L;

            CasSimpleMultifactorAuthenticationTicketStringSerializer(final ConfigurableApplicationContext applicationContext) {
                super(MINIMAL_PRETTY_PRINTER, applicationContext, CasSimpleMultifactorAuthenticationTicketImpl.class);
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
}
