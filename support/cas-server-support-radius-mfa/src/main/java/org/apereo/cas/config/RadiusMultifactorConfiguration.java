package org.apereo.cas.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowAction;
import org.apereo.cas.adaptors.radius.web.flow.RadiusAuthenticationWebflowEventResolver;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
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
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link RadiusMultifactorConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Nagai Takayuki
 * @since 5.0.0
 */
@Configuration("radiusMfaConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.authn.mfa.radius.client.inet-address")
public class RadiusMultifactorConfiguration {
    private static final int WEBFLOW_CONFIGURER_ORDER = 100;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("authenticationContextValidator")
    private ObjectProvider<MultifactorAuthenticationContextValidator> authenticationContextValidator;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("flowBuilder")
    private ObjectProvider<FlowBuilder> flowBuilder;

    @Bean
    @ConditionalOnMissingBean(name = "radiusFlowRegistry")
    public FlowDefinitionRegistry radiusFlowRegistry() {
        val builder = new FlowDefinitionRegistryBuilder(this.applicationContext, flowBuilderServices.getObject());
        builder.addFlowBuilder(flowBuilder.getObject(), RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID);
        return builder.build();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowAction")
    public Action radiusAuthenticationWebflowAction() {
        return new RadiusAuthenticationWebflowAction(radiusAuthenticationWebflowEventResolver());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "radiusAuthenticationWebflowEventResolver")
    public CasWebflowEventResolver radiusAuthenticationWebflowEventResolver() {
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
            .authenticationEventExecutionPlan(authenticationEventExecutionPlan.getObject())
            .build();

        return new RadiusAuthenticationWebflowEventResolver(context,
            casProperties.getAuthn().getMfa().getRadius().getAllowedAuthenticationAttempts());
    }

    @ConditionalOnMissingBean(name = "radiusMultifactorWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer radiusMultifactorWebflowConfigurer() {
        val cfg = new RadiusMultifactorWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(),
            radiusFlowRegistry(),
            applicationContext,
            casProperties,
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @Bean
    @ConditionalOnMissingBean(name = "radiusMultifactorCasWebflowExecutionPlanConfigurer")
    public CasWebflowExecutionPlanConfigurer radiusMultifactorCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(radiusMultifactorWebflowConfigurer());
    }

    /**
     * The Radius multifactor trust configuration.
     */
    @ConditionalOnClass(value = MultifactorAuthnTrustConfiguration.class)
    @ConditionalOnProperty(prefix = "cas.authn.mfa.radius", name = "trusted-device-enabled", havingValue = "true", matchIfMissing = true)
    @Configuration("radiusMultifactorTrustConfiguration")
    public class RadiusMultifactorTrustConfiguration {

        @ConditionalOnMissingBean(name = "radiusMultifactorTrustConfigurer")
        @Bean
        @DependsOn("defaultWebflowConfigurer")
        public CasWebflowConfigurer radiusMultifactorTrustConfigurer() {
            val cfg = new RadiusMultifactorTrustedDeviceWebflowConfigurer(flowBuilderServices.getObject(),
                loginFlowDefinitionRegistry.getObject(),
                radiusFlowRegistry(),
                applicationContext,
                casProperties,
                MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
            cfg.setOrder(WEBFLOW_CONFIGURER_ORDER + 1);
            return cfg;
        }

        @Bean
        public CasWebflowExecutionPlanConfigurer radiusMultifactorTrustCasWebflowExecutionPlanConfigurer() {
            return plan -> plan.registerWebflowConfigurer(radiusMultifactorTrustConfigurer());
        }

    }
}
