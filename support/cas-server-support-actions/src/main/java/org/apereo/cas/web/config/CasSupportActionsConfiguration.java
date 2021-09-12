package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
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
import org.apereo.cas.web.flow.logout.ConfirmLogoutAction;
import org.apereo.cas.web.flow.logout.FinishLogoutAction;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.flow.logout.LogoutAction;
import org.apereo.cas.web.flow.logout.LogoutViewSetupAction;
import org.apereo.cas.web.flow.logout.TerminateSessionAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.ArgumentExtractor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
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
@Configuration(value = "casSupportActionsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class CasSupportActionsConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(LogoutManager.DEFAULT_BEAN_NAME)
    private ObjectProvider<LogoutManager> logoutManager;

    @Autowired
    @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
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
    private ObjectProvider<ServiceFactory<WebApplicationService>> webApplicationServiceFactory;

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
    @Qualifier("defaultSingleLogoutRequestExecutor")
    private ObjectProvider<SingleLogoutRequestExecutor> defaultSingleLogoutRequestExecutor;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

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

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("logoutExecutionPlan")
    private ObjectProvider<LogoutExecutionPlan> logoutExecutionPlan;

    @Bean
    @RefreshScope
    public HandlerExceptionResolver errorHandlerResolver() {
        return new FlowExecutionExceptionResolver();
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION)
    @Bean
    @RefreshScope
    public Action authenticationViaFormAction() {
        return new InitialAuthenticationAction(
            initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK)
    @Bean
    public Action serviceAuthorizationCheck() {
        return new ServiceAuthorizationCheckAction(this.servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET)
    @Bean
    public Action sendTicketGrantingTicketAction() {
        return new SendTicketGrantingTicketAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            webflowSingleSignOnParticipationStrategy.getObject());
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET)
    @Bean
    public Action createTicketGrantingTicketAction() {
        return new CreateTicketGrantingTicketAction(casWebflowConfigurationContext.getObject());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
    @Bean
    @RefreshScope
    public Action finishLogoutAction() {
        return new FinishLogoutAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(), argumentExtractor.getObject(),
            servicesManager.getObject(), logoutExecutionPlan.getObject(), casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT)
    public Action logoutAction() {
        return new LogoutAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(), argumentExtractor.getObject(),
            servicesManager.getObject(), logoutExecutionPlan.getObject(), casProperties);
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION)
    @Bean
    @RefreshScope
    public Action initializeLoginAction() {
        return new InitializeLoginAction(servicesManager.getObject(), casProperties);
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "setServiceUnauthorizedRedirectUrlAction")
    @Bean
    public Action setServiceUnauthorizedRedirectUrlAction() {
        return new SetServiceUnauthorizedRedirectUrlAction(servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM)
    @Bean
    @RefreshScope
    public Action renderLoginFormAction() {
        return new RenderLoginAction(servicesManager.getObject(), casProperties, applicationContext);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
    public Action initialFlowSetupAction() {
        return new InitialFlowSetupAction(
            CollectionUtils.wrap(argumentExtractor.getObject()),
            servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            warnCookieGenerator.getObject(),
            casProperties,
            authenticationEventExecutionPlan.getObject(),
            webflowSingleSignOnParticipationStrategy.getObject(),
            ticketRegistrySupport.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE)
    public Action verifyRequiredServiceAction() {
        return new VerifyRequiredServiceAction(
            servicesManager.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            casProperties,
            ticketRegistrySupport.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION)
    public Action initialAuthenticationRequestValidationAction() {
        return new InitialAuthenticationRequestValidationAction(rankedAuthenticationProviderWebflowEventResolver.getObject());
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "genericSuccessViewAction")
    public Action genericSuccessViewAction() {
        return new GenericSuccessViewAction(centralAuthenticationService.getObject(),
            servicesManager.getObject(),
            webApplicationServiceFactory.getObject(),
            casProperties);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "redirectUnauthorizedServiceUrlAction")
    public Action redirectUnauthorizedServiceUrlAction() {
        return new RedirectUnauthorizedServiceUrlAction(servicesManager.getObject(), applicationContext);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET)
    public Action generateServiceTicketAction() {
        return new GenerateServiceTicketAction(authenticationSystemSupport.getObject(),
            centralAuthenticationService.getObject(),
            ticketRegistrySupport.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject(),
            servicesManager.getObject(),
            principalElectionStrategy.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "gatewayServicesManagementCheck")
    @RefreshScope
    public Action gatewayServicesManagementCheck() {
        return new GatewayServicesManagementCheckAction(this.servicesManager.getObject(),
            authenticationRequestServiceSelectionStrategies.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "frontChannelLogoutAction")
    public Action frontChannelLogoutAction() {
        return new FrontChannelLogoutAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(), argumentExtractor.getObject(),
            servicesManager.getObject(), logoutExecutionPlan.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK)
    public Action ticketGrantingTicketCheckAction() {
        return new TicketGrantingTicketCheckAction(this.centralAuthenticationService.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
    @RefreshScope
    public Action terminateSessionAction() {
        return new TerminateSessionAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(),
            warnCookieGenerator.getObject(),
            casProperties.getLogout(),
            logoutManager.getObject(),
            applicationContext,
            defaultSingleLogoutRequestExecutor.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "confirmLogoutAction")
    @RefreshScope
    public Action confirmLogoutAction() {
        return new ConfirmLogoutAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(), argumentExtractor.getObject(),
            servicesManager.getObject(), logoutExecutionPlan.getObject(), casProperties);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
    public Action logoutViewSetupAction() {
        return new LogoutViewSetupAction(centralAuthenticationService.getObject(),
            ticketGrantingTicketCookieGenerator.getObject(), argumentExtractor.getObject(),
            servicesManager.getObject(), logoutExecutionPlan.getObject(), casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SERVICE_WARNING)
    @RefreshScope
    public Action serviceWarningAction() {
        return new ServiceWarningAction(centralAuthenticationService.getObject(),
            authenticationSystemSupport.getObject(),
            ticketRegistrySupport.getObject(),
            warnCookieGenerator.getObject(),
            principalElectionStrategy.getObject());
    }
}
