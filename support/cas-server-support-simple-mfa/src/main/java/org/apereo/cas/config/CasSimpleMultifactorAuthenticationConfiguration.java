package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator;
import org.apereo.cas.mfa.simple.ticket.DefaultCasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorSendTokenAction;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
 * This is {@link CasSimpleMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "casSimpleMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class CasSimpleMultifactorAuthenticationConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Configuration(value = "CasSimpleMultifactorAuthenticationActionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationActionConfiguration {
        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorSendTokenAction")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public Action mfaSimpleMultifactorSendTokenAction(
            @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
            final CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory,
            @Qualifier("mfaSimpleMultifactorTokenCommunicationStrategy")
            final CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy,
            final CasConfigurationProperties casProperties,
            @Qualifier(TicketRegistry.BEAN_NAME)
            final TicketRegistry ticketRegistry,
            @Qualifier("communicationsManager")
            final CommunicationsManager communicationsManager) {
            val simple = casProperties.getAuthn().getMfa().getSimple();
            return new CasSimpleMultifactorSendTokenAction(ticketRegistry,
                communicationsManager, casSimpleMultifactorAuthenticationTicketFactory, simple,
                mfaSimpleMultifactorTokenCommunicationStrategy);
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationPlanConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "mfaSimpleCasWebflowExecutionPlanConfigurer")
        @Autowired
        public CasWebflowExecutionPlanConfigurer mfaSimpleCasWebflowExecutionPlanConfigurer(
            @Qualifier("mfaSimpleMultifactorWebflowConfigurer")
            final CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorWebflowConfigurer);
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationBaseConfiguration {
        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
        @Bean
        @Autowired
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
    public static class CasSimpleMultifactorAuthenticationWebflowConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "mfaSimpleAuthenticatorFlowRegistry")
        @Autowired
        public FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry(
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER)
            final FlowBuilder flowBuilder,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final ConfigurableApplicationContext applicationContext) {
            val builder = new FlowDefinitionRegistryBuilder(applicationContext, flowBuilderServices);
            builder.addFlowBuilder(flowBuilder, CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_EVENT_ID);
            return builder.build();
        }

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTokenCommunicationStrategy")
        @Bean
        public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
            return CasSimpleMultifactorTokenCommunicationStrategy.all();
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationTicketConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketExpirationPolicy")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy(final CasConfigurationProperties casProperties) {
            return new CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder(casProperties);
        }

        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator(final CasConfigurationProperties casProperties) {
            val simple = casProperties.getAuthn().getMfa().getSimple();
            return new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(simple.getTokenLength());
        }
    }

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketFactoryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationTicketFactoryConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
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

    @Configuration(value = "CasSimpleMultifactorAuthenticationTicketFactoryPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasSimpleMultifactorAuthenticationTicketFactoryPlanConfiguration {
        @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactoryConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public TicketFactoryExecutionPlanConfigurer casSimpleMultifactorAuthenticationTicketFactoryConfigurer(
            @Qualifier("casSimpleMultifactorAuthenticationTicketFactory")
            final CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory) {
            return () -> casSimpleMultifactorAuthenticationTicketFactory;
        }
    }

    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @Configuration(value = "casSimpleMultifactorTrustConfiguration", proxyBeanMethods = false)
    @DependsOn("casSimpleMultifactorAuthenticationTicketFactoryConfigurer")
    public static class CasSimpleMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTrustWebflowConfigurer")
        @Bean
        @Autowired
        public CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer(
            @Qualifier("mfaSimpleAuthenticatorFlowRegistry")
            final FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val cfg = new CasSimpleMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices,
                loginFlowDefinitionRegistry,
                mfaSimpleAuthenticatorFlowRegistry,
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @ConditionalOnMissingBean(name = "casSimpleMultifactorTrustWebflowExecutionPlanConfigurer")
        @Bean
        @Autowired
        public CasWebflowExecutionPlanConfigurer casSimpleMultifactorTrustWebflowExecutionPlanConfigurer(
            @Qualifier("mfaSimpleMultifactorTrustWebflowConfigurer")
            final CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer) {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorTrustWebflowConfigurer);
        }
    }
}
