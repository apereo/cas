package org.apereo.cas.adaptors.authy.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowAction;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
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
 * This is {@link AuthyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("authyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;
    
    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<MultifactorAuthenticationContextValidator> authenticationContextValidator;
    
    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Bean
    public FlowDefinitionRegistry authyAuthenticatorFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.setBasePath(CasWebflowConstants.BASE_CLASSPATH_WEBFLOW);
        builder.addFlowLocationPattern("/mfa-authy/*-webflow.xml");
        return builder.build();
    }

    @RefreshScope
    @Bean
    public CasWebflowEventResolver authyAuthenticationWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .casDelegatingWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver.getObject())
            .authenticationContextValidator(authenticationContextValidator.getObject())
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .warnCookieGenerator(warnCookieGenerator.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .casProperties(casProperties)
            .singleSignOnParticipationStrategy(webflowSingleSignOnParticipationStrategy.getObject())
            .ticketRegistry(ticketRegistry.getObject())
            .applicationContext(applicationContext)
            .authenticationEventExecutionPlan(authenticationEventExecutionPlan.getObject())
            .build();

        return new AuthyAuthenticationWebflowEventResolver(context);
    }

    @ConditionalOnMissingBean(name = "authyMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer authyMultifactorWebflowConfigurer() {
        val cfg = new AuthyMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), authyAuthenticatorFlowRegistry(),
            applicationContext, casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @RefreshScope
    @Bean
    public Action authyAuthenticationWebflowAction() {
        return new AuthyAuthenticationWebflowAction(authyAuthenticationWebflowEventResolver());
    }

    @Bean
    @ConditionalOnMissingBean(name = "authyCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer authyCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(authyMultifactorWebflowConfigurer());
    }

    /**
     * The Authy multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.authy", name = "trusted-device-enabled", havingValue = "true", matchIfMissing = true)
    @Configuration("authyMultifactorTrustConfiguration")
    public class AuthyMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "authyMultifactorTrustWebflowConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer authyMultifactorTrustWebflowConfigurer() {
            val cfg = new AuthyMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                authyAuthenticatorFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        @ConditionalOnMissingBean(name = "authyMultifactorTrustCasWebflowExecutionPlanConfigurer")
        public CasWebflowExecutionPlanConfigurer authyMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(authyMultifactorTrustWebflowConfigurer());
        }
    }
}
