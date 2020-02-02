package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowEventResolver;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorTrustWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationAction;

import com.yubico.webauthn.RegistrationStorage;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.config.FlowDefinitionRegistryBuilder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link WebAuthnWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("webAuthnWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebAuthnWebflowConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private ObjectProvider<RegistrationStorage> webAuthnCredentialRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Bean
    public FlowDefinitionRegistry webAuthnFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, this.flowBuilderServices);
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-webauthn/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowAction")
    @Bean
    public Action webAuthnAuthenticationWebflowAction() {
        return new WebAuthnAuthenticationWebflowAction(webAuthnAuthenticationWebflowEventResolver());
    }

    @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer() {
        return new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry.getIfAvailable(), webAuthnFlowRegistry(),
            applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "webAuthnStartAuthenticationAction")
    @Bean
    public Action webAuthnStartAuthenticationAction() {
        return new WebAuthnStartAuthenticationAction(webAuthnCredentialRepository.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "webAuthnStartRegistrationAction")
    @Bean
    public Action webAuthnStartRegistrationAction() {
        return new WebAuthnStartRegistrationAction(webAuthnCredentialRepository.getIfAvailable(), casProperties);
    }

    @ConditionalOnMissingBean(name = "webAuthnCheckAccountRegistrationAction")
    @Bean
    public Action webAuthnCheckAccountRegistrationAction() {
        return new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "webAuthnSaveAccountRegistrationAction")
    @Bean
    public Action webAuthnSaveAccountRegistrationAction() {
        return new WebAuthnAccountSaveRegistrationAction(webAuthnCredentialRepository.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
    @Bean
    public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getIfAvailable())
            .centralAuthenticationService(centralAuthenticationService.getIfAvailable())
            .servicesManager(servicesManager.getIfAvailable())
            .ticketRegistrySupport(ticketRegistrySupport.getIfAvailable())
            .warnCookieGenerator(warnCookieGenerator.getIfAvailable())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getIfAvailable())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getIfAvailable())
            .casProperties(casProperties)
            .eventPublisher(applicationEventPublisher)
            .applicationContext(applicationContext)
            .build();

        return new WebAuthnAuthenticationWebflowEventResolver(context);
    }

    @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustConfiguration")
    @Bean
    public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorWebflowConfigurer());
    }

    /**
     * The WebAuthN multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.webAuthn", name = "trustedDeviceEnabled", havingValue = "true", matchIfMissing = true)
    @Configuration("webAuthnMultifactorTrustConfiguration")
    public class WebAuthnMultifactorTrustConfiguration implements CasWebflowExecutionPlanConfigurer {

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer() {
            val deviceRegistrationEnabled = casProperties.getAuthn().getMfa().getTrusted().isDeviceRegistrationEnabled();
            return new WebAuthnMultifactorTrustWebflowConfigurer(flowBuilderServices,
                deviceRegistrationEnabled, loginFlowDefinitionRegistry.getIfAvailable(),
                applicationContext, casProperties, webAuthnFlowRegistry());
        }

        @Override
        public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
            plan.registerWebflowConfigurer(webAuthnMultifactorTrustWebflowConfigurer());
        }
    }
}
