package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.flow.InitializeLogoutViewAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.FrontChannelLogoutAction;
import org.apereo.cas.web.flow.GatewayServicesManagementCheck;
import org.apereo.cas.web.flow.GenerateServiceTicketAction;
import org.apereo.cas.web.flow.GenericSuccessViewAction;
import org.apereo.cas.web.flow.actions.InitialAuthenticationAction;
import org.apereo.cas.web.flow.InitialAuthenticationRequestValidationAction;
import org.apereo.cas.web.flow.InitialFlowSetupAction;
import org.apereo.cas.web.flow.InitializeLoginAction;
import org.apereo.cas.web.flow.LogoutAction;
import org.apereo.cas.web.flow.SendTicketGrantingTicketAction;
import org.apereo.cas.web.flow.ServiceAuthorizationCheck;
import org.apereo.cas.web.flow.ServiceWarningAction;
import org.apereo.cas.web.flow.TerminateSessionAction;
import org.apereo.cas.web.flow.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSupportActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class CasSupportActionsConfiguration {

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieRetrievingCookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy webflowSingleSignOnParticipationStrategy;

    @Bean
    @RefreshScope
    public HandlerExceptionResolver errorHandlerResolver() {
        return new FlowExecutionExceptionResolver();
    }

    @ConditionalOnMissingBean(name = "authenticationViaFormAction")
    @Bean
    @RefreshScope
    public Action authenticationViaFormAction() {
        return new InitialAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "serviceAuthorizationCheck")
    @Bean
    public Action serviceAuthorizationCheck() {
        return new ServiceAuthorizationCheck(this.servicesManager, authenticationRequestServiceSelectionStrategies);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "sendTicketGrantingTicketAction")
    @Bean
    public Action sendTicketGrantingTicketAction() {
        return new SendTicketGrantingTicketAction(centralAuthenticationService,
                ticketGrantingTicketCookieGenerator, webflowSingleSignOnParticipationStrategy);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "logoutAction")
    public Action logoutAction() {
        return new LogoutAction(webApplicationServiceFactory, servicesManager, casProperties.getLogout());
    }

    @ConditionalOnMissingBean(name = "initializeLoginAction")
    @Bean
    @RefreshScope
    public Action initializeLoginAction() {
        return new InitializeLoginAction(servicesManager);
    }

    @RefreshScope
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "initialFlowSetupAction")
    public Action initialFlowSetupAction(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        return new InitialFlowSetupAction(CollectionUtils.wrap(argumentExtractor),
                servicesManager,
                authenticationRequestServiceSelectionStrategies,
                ticketGrantingTicketCookieGenerator,
                warnCookieGenerator, casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "initialAuthenticationRequestValidationAction")
    public Action initialAuthenticationRequestValidationAction() {
        return new InitialAuthenticationRequestValidationAction(rankedAuthenticationProviderWebflowEventResolver);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "genericSuccessViewAction")
    public Action genericSuccessViewAction() {
        return new GenericSuccessViewAction(centralAuthenticationService, servicesManager, webApplicationServiceFactory,
                casProperties.getView().getDefaultRedirectUrl());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "generateServiceTicketAction")
    public Action generateServiceTicketAction() {
        return new GenerateServiceTicketAction(authenticationSystemSupport,
                centralAuthenticationService,
                ticketRegistrySupport,
                servicesManager,
                authenticationRequestServiceSelectionStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(name = "gatewayServicesManagementCheck")
    @RefreshScope
    public Action gatewayServicesManagementCheck() {
        return new GatewayServicesManagementCheck(this.servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "frontChannelLogoutAction")
    public Action frontChannelLogoutAction() {
        return new FrontChannelLogoutAction(this.logoutManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketGrantingTicketCheckAction")
    public Action ticketGrantingTicketCheckAction() {
        return new TicketGrantingTicketCheckAction(this.centralAuthenticationService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "terminateSessionAction")
    @RefreshScope
    public Action terminateSessionAction() {
        return new TerminateSessionAction(centralAuthenticationService, ticketGrantingTicketCookieGenerator,
                warnCookieGenerator, casProperties.getLogout());
    }

    @Bean
    @RefreshScope
    public Action initializeLogoutViewAction() {
        return new InitializeLogoutViewAction(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceWarningAction")
    @RefreshScope
    public Action serviceWarningAction() {
        return new ServiceWarningAction(centralAuthenticationService, authenticationSystemSupport, ticketRegistrySupport, warnCookieGenerator);
    }
}
