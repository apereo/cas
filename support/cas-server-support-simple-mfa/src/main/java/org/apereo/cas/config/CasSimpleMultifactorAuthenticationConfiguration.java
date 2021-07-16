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
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("casSimpleMultifactorAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableScheduling
public class CasSimpleMultifactorAuthenticationConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Bean
    @ConditionalOnMissingBean(name = "mfaSimpleAuthenticatorFlowRegistry")
    public FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(), CasSimpleMultifactorWebflowConfigurer.MFA_SIMPLE_EVENT_ID);
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        val cfg = new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            mfaSimpleAuthenticatorFlowRegistry(), applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @ConditionalOnMissingBean(name = "mfaSimpleCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer mfaSimpleCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorWebflowConfigurer());
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorSendTokenAction")
    @Bean
    @RefreshScope
    public Action mfaSimpleMultifactorSendTokenAction() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new CasSimpleMultifactorSendTokenAction(ticketRegistry.getObject(),
            communicationsManager.getObject(),
            casSimpleMultifactorAuthenticationTicketFactory(), simple,
            mfaSimpleMultifactorTokenCommunicationStrategy());
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTokenCommunicationStrategy")
    @Bean
    public CasSimpleMultifactorTokenCommunicationStrategy mfaSimpleMultifactorTokenCommunicationStrategy() {
        return CasSimpleMultifactorTokenCommunicationStrategy.all();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketExpirationPolicy")
    @Bean
    @RefreshScope
    public ExpirationPolicyBuilder casSimpleMultifactorAuthenticationTicketExpirationPolicy() {
        return new CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder(casProperties);
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationUniqueTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator casSimpleMultifactorAuthenticationUniqueTicketIdGenerator() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator(simple.getTokenLength());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactory")
    @Bean
    @RefreshScope
    public CasSimpleMultifactorAuthenticationTicketFactory casSimpleMultifactorAuthenticationTicketFactory() {
        return new DefaultCasSimpleMultifactorAuthenticationTicketFactory(
            casSimpleMultifactorAuthenticationTicketExpirationPolicy(),
            casSimpleMultifactorAuthenticationUniqueTicketIdGenerator());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactoryConfigurer")
    @Bean
    @RefreshScope
    public TicketFactoryExecutionPlanConfigurer casSimpleMultifactorAuthenticationTicketFactoryConfigurer() {
        return this::casSimpleMultifactorAuthenticationTicketFactory;
    }

    /**
     * The simple multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @Configuration("casSimpleMultifactorTrustConfiguration")
    public class CasSimpleMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer() {
            val cfg = new CasSimpleMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                mfaSimpleAuthenticatorFlowRegistry(),
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @ConditionalOnMissingBean(name = "casSimpleMultifactorTrustWebflowExecutionPlanConfigurer")
        @Bean
        public CasWebflowExecutionPlanConfigurer casSimpleMultifactorTrustWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorTrustWebflowConfigurer());
        }
    }
}
