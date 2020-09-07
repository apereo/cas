package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.trusted.config.MultifactorAuthnTrustConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountCheckRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAccountSaveRegistrationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationWebflowEventResolver;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorTrustWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnMultifactorWebflowConfigurer;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartAuthenticationAction;
import org.apereo.cas.webauthn.web.flow.WebAuthnStartRegistrationAction;

import com.yubico.webauthn.core.RegistrationStorage;
import com.yubico.webauthn.core.SessionManager;
import lombok.val;
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
@Configuration(value = "webAuthnWebflowConfiguration", proxyBeanMethods = true)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebAuthnWebflowConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webAuthnSessionManager")
    private ObjectProvider<SessionManager> webAuthnSessionManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private ObjectProvider<RegistrationStorage> webAuthnCredentialRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

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

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<MultifactorAuthenticationContextValidator> authenticationContextValidator;

    @Bean
    public FlowDefinitionRegistry webAuthnFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-webauthn/*-webflow.xml");
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowAction")
    @Bean
    @RefreshScope
    @Autowired
    public Action webAuthnAuthenticationWebflowAction(@Qualifier("webAuthnAuthenticationWebflowEventResolver")
                                                      final CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver) {
        return new WebAuthnAuthenticationWebflowAction(webAuthnAuthenticationWebflowEventResolver);
    }

    @ConditionalOnMissingBean(name = "webAuthnMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer() {
        val cfg = new WebAuthnMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), webAuthnFlowRegistry(),
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @ConditionalOnMissingBean(name = "webAuthnStartAuthenticationAction")
    @Bean
    @RefreshScope
    public Action webAuthnStartAuthenticationAction() {
        return new WebAuthnStartAuthenticationAction(webAuthnCredentialRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnStartRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnStartRegistrationAction() {
        return new WebAuthnStartRegistrationAction(webAuthnCredentialRepository.getObject(), casProperties);
    }

    @ConditionalOnMissingBean(name = "webAuthnCheckAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnCheckAccountRegistrationAction() {
        return new WebAuthnAccountCheckRegistrationAction(webAuthnCredentialRepository.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnSaveAccountRegistrationAction")
    @Bean
    @RefreshScope
    public Action webAuthnSaveAccountRegistrationAction() {
        return new WebAuthnAccountSaveRegistrationAction(webAuthnCredentialRepository.getObject(),
            webAuthnSessionManager.getObject());
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .casDelegatingWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver.getObject())
            .authenticationContextValidator(authenticationContextValidator.getObject())
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .servicesManager(servicesManager.getObject())
            .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .warnCookieGenerator(warnCookieGenerator.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .casProperties(casProperties)
            .ticketRegistry(ticketRegistry.getObject())
            .applicationContext(applicationContext)
            .build();
        return new WebAuthnAuthenticationWebflowEventResolver(context);
    }

    @ConditionalOnMissingBean(name = "webAuthnCasWebflowExecutionPlanConfigurer")
    @Bean
    @Autowired
    public CasWebflowExecutionPlanConfigurer webAuthnCasWebflowExecutionPlanConfigurer(@Qualifier("webAuthnMultifactorWebflowConfigurer")
                                                                                       final CasWebflowConfigurer webAuthnMultifactorWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorWebflowConfigurer);
    }

    /**
     * The WebAuthN multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.web-authn", name = "trusted-device-enabled", havingValue = "true", matchIfMissing = true)
    @Configuration("webAuthnMultifactorTrustConfiguration")
    public class WebAuthnMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "webAuthnMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer webAuthnMultifactorTrustWebflowConfigurer() {
            val cfg = new WebAuthnMultifactorTrustWebflowConfigurer(
                flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                webAuthnFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer webAuthnMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(webAuthnMultifactorTrustWebflowConfigurer());
        }
    }
}
