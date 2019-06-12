package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.GatewayServicesManagementCheckAction;
import org.apereo.cas.web.flow.GenerateServiceTicketAction;
import org.apereo.cas.web.flow.ServiceAuthorizationCheckAction;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.InitialAuthenticationAction;
import org.apereo.cas.web.flow.login.CreateTicketGrantingTicketAction;
import org.apereo.cas.web.flow.login.GenericSuccessViewAction;
import org.apereo.cas.web.flow.login.InitialAuthenticationRequestValidationAction;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.flow.login.InitializeLoginAction;
import org.apereo.cas.web.flow.login.RedirectUnauthorizedServiceUrlAction;
import org.apereo.cas.web.flow.login.RenderLoginAction;
import org.apereo.cas.web.flow.login.SendTicketGrantingTicketAction;
import org.apereo.cas.web.flow.login.ServiceWarningAction;
import org.apereo.cas.web.flow.login.SetServiceUnauthorizedRedirectUrlAction;
import org.apereo.cas.web.flow.login.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.flow.login.VerifyRequiredServiceAction;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.flow.logout.LogoutAction;
import org.apereo.cas.web.flow.logout.LogoutViewSetupAction;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
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
    private ApplicationContext applicationContext;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ObjectProvider<ServiceFactory> webApplicationServiceFactory;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

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
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> rankedAuthenticationProviderWebflowEventResolver;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private ObjectProvider<SingleSignOnParticipationStrategy> webflowSingleSignOnParticipationStrategy;

    @Autowired
    @Qualifier("principalElectionStrategy")
    private ObjectProvider<PrincipalElectionStrategy> principalElectionStrategy;

    @Bean
    @RefreshScope
    public HandlerExceptionResolver errorHandlerResolver() {
        return new FlowExecutionExceptionResolver();
    }

    @ConditionalOnMissingBean(name = "authenticationViaFormAction")
    @Bean
    @RefreshScope
    public Action authenticationViaFormAction() {
        return new InitialAuthenticationAction(
            initialAuthenticationAttemptWebflowEventResolver.getIfAvailable(),
            serviceTicketRequestWebflowEventResolver.getIfAvailable(),
            adaptiveAuthenticationPolicy.getIfAvailable());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "serviceAuthorizationCheck")
    @Bean
    public Action serviceAuthorizationCheck() {
        return new ServiceAuthorizationCheckAction(this.servicesManager.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "sendTicketGrantingTicketAction")
    @Bean
    public Action sendTicketGrantingTicketAction() {
        return new SendTicketGrantingTicketAction(centralAuthenticationService.getIfAvailable(),
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            webflowSingleSignOnParticipationStrategy.getIfAvailable());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "createTicketGrantingTicketAction")
    @Bean
    public Action createTicketGrantingTicketAction() {
        return new CreateTicketGrantingTicketAction(centralAuthenticationService.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "logoutAction")
    public Action logoutAction() {
        return new LogoutAction(webApplicationServiceFactory.getIfAvailable(),
            servicesManager.getIfAvailable(),
            casProperties.getLogout());
    }

    @ConditionalOnMissingBean(name = "initializeLoginAction")
    @Bean
    @RefreshScope
    public Action initializeLoginAction() {
        return new InitializeLoginAction(servicesManager.getIfAvailable());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "setServiceUnauthorizedRedirectUrlAction")
    @Bean
    public Action setServiceUnauthorizedRedirectUrlAction() {
        return new SetServiceUnauthorizedRedirectUrlAction(servicesManager.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "renderLoginFormAction")
    @Bean
    @RefreshScope
    public Action renderLoginFormAction() {
        return new RenderLoginAction(servicesManager.getIfAvailable(), casProperties, applicationContext);
    }

    @RefreshScope
    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "initialFlowSetupAction")
    public Action initialFlowSetupAction(@Qualifier("argumentExtractor") final ArgumentExtractor argumentExtractor) {
        return new InitialFlowSetupAction(CollectionUtils.wrap(argumentExtractor),
            servicesManager.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            casProperties,
            authenticationEventExecutionPlan.getIfAvailable(),
            webflowSingleSignOnParticipationStrategy.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "verifyRequiredServiceAction")
    public Action verifyRequiredServiceAction() {
        return new VerifyRequiredServiceAction(
            servicesManager.getIfAvailable(),
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            casProperties,
            ticketRegistrySupport.getIfAvailable());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "initialAuthenticationRequestValidationAction")
    public Action initialAuthenticationRequestValidationAction() {
        return new InitialAuthenticationRequestValidationAction(rankedAuthenticationProviderWebflowEventResolver.getIfAvailable());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "genericSuccessViewAction")
    public Action genericSuccessViewAction() {
        return new GenericSuccessViewAction(centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            webApplicationServiceFactory.getIfAvailable(),
            casProperties.getView().getDefaultRedirectUrl());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redirectUnauthorizedServiceUrlAction")
    public Action redirectUnauthorizedServiceUrlAction() {
        return new RedirectUnauthorizedServiceUrlAction(servicesManager.getIfAvailable(), resourceLoader, applicationContext);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "generateServiceTicketAction")
    public Action generateServiceTicketAction() {
        return new GenerateServiceTicketAction(authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable(),
            servicesManager.getIfAvailable(),
            principalElectionStrategy.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "gatewayServicesManagementCheck")
    @RefreshScope
    public Action gatewayServicesManagementCheck() {
        return new GatewayServicesManagementCheckAction(this.servicesManager.getIfAvailable(),
            authenticationRequestServiceSelectionStrategies.getIfAvailable());
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "frontChannelLogoutAction")
    public Action frontChannelLogoutAction(@Qualifier("logoutExecutionPlan") final LogoutExecutionPlan logoutExecutionPlan) {
        return new FrontChannelLogoutAction(logoutExecutionPlan, casProperties.getSlo().isDisabled());
    }

    @Bean
    @ConditionalOnMissingBean(name = "ticketGrantingTicketCheckAction")
    public Action ticketGrantingTicketCheckAction() {
        return new TicketGrantingTicketCheckAction(this.centralAuthenticationService.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "terminateSessionAction")
    @RefreshScope
    public Action terminateSessionAction() {
        return new TerminateSessionAction(centralAuthenticationService.getIfAvailable(),
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            casProperties.getLogout());
    }

    @Bean
    @ConditionalOnMissingBean(name = "logoutViewSetupAction")
    public Action logoutViewSetupAction() {
        return new LogoutViewSetupAction(casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceWarningAction")
    @RefreshScope
    public Action serviceWarningAction() {
        return new ServiceWarningAction(centralAuthenticationService.getIfAvailable(),
            authenticationSystemSupport.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            principalElectionStrategy.getIfAvailable());
    }
}
