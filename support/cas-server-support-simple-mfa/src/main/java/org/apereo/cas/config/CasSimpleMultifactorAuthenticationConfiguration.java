package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketExpirationPolicyBuilder;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationUniqueTicketIdGenerator;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorTrustWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorWebflowConfigurer;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleSendTokenAction;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Objects;

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

    @Bean
    public FlowDefinitionRegistry mfaSimpleAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices.getObject());
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-simple/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "mfaSimpleMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer mfaSimpleMultifactorWebflowConfigurer() {
        return new CasSimpleMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            mfaSimpleAuthenticatorFlowRegistry(), applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
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
        if (!Objects.requireNonNull(communicationsManager.getObject()).validate()) {
            throw new BeanCreationException("Unable to submit tokens since no communication strategy is defined");
        }
        return new CasSimpleSendTokenAction(ticketRegistry.getObject(), communicationsManager.getObject(),
            casSimpleMultifactorAuthenticationTicketFactory(), simple);
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
    public TransientSessionTicketFactory casSimpleMultifactorAuthenticationTicketFactory() {
        return new CasSimpleMultifactorAuthenticationTicketFactory(casSimpleMultifactorAuthenticationTicketExpirationPolicy(),
            casSimpleMultifactorAuthenticationUniqueTicketIdGenerator());
    }

    /**
     * The simple multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.simple", name = "trusted-device-enabled", havingValue = "true", matchIfMissing = true)
    @Configuration("casSimpleMultifactorTrustConfiguration")
    public class CasSimpleMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "mfaSimpleMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer mfaSimpleMultifactorTrustWebflowConfigurer() {
            return new CasSimpleMultifactorTrustWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled(),
                mfaSimpleAuthenticatorFlowRegistry(),
                applicationContext, casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer casSimpleMultifactorTrustWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(mfaSimpleMultifactorTrustWebflowConfigurer());
        }
    }
}
