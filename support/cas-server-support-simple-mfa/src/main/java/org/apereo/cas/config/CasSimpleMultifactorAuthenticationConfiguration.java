package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorTrustWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleSendTokenAction;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;

import lombok.val;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
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
public class CasSimpleMultifactorAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Bean
    public FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath("classpath*:/webflow");
        builder.addFlowLocationPattern("/mfa-simple/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        return new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
            mfaSimpleAuthenticatorFlowRegistry(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorSendTokenAction")
    @Bean
    public Action mfaSimpleMultifactorSendTokenAction() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        if (!communicationsManager.validate()) {
            throw new BeanCreationException("Unable to submit tokens since no communication strategy is defined");
        }
        return new CasSimpleSendTokenAction(ticketRegistry, communicationsManager,
            casSimpleMultifactorAuthenticationTicketFactory(), simple);
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(mfaSimpleMultifactorWebflowConfigurer());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketExpirationPolicy")
    @Bean
    public ExpirationPolicy casSimpleMultifactorAuthenticationTicketExpirationPolicy() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new HardTimeoutExpirationPolicy(simple.getTimeToKillInSeconds());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationTicketFactory")
    @Bean
    public TransientSessionTicketFactory casSimpleMultifactorAuthenticationTicketFactory() {
        return new CasSimpleMultifactorAuthenticationTicketFactory(casSimpleMultifactorAuthenticationTicketExpirationPolicy());
    }

    /**
     * The simple multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.simple", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("casSimpleMultifactorTrustConfiguration")
    public class CasSimpleMultifactorTrustConfiguration implements CasWebflowExecutionPlanConfigurer {

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer() {
            return new CasSimpleMultifactorTrustWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(), mfaSimpleAuthenticatorFlowRegistry(),
                applicationContext, casProperties);
        }

        @Override
        public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
            plan.registerWebflowConfigurer(mfaSimpleMultifactorTrustWebflowConfigurer());
        }
    }
}
