package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.flow.AbstractAuthenticationAction;
import org.apereo.cas.web.flow.FrontChannelLogoutAction;
import org.apereo.cas.web.flow.GatewayServicesManagementCheck;
import org.apereo.cas.web.flow.GenerateServiceTicketAction;
import org.apereo.cas.web.flow.GenericSuccessViewAction;
import org.apereo.cas.web.flow.InitialAuthenticationRequestValidationAction;
import org.apereo.cas.web.flow.InitialFlowSetupAction;
import org.apereo.cas.web.flow.InitializeLoginAction;
import org.apereo.cas.web.flow.LogoutAction;
import org.apereo.cas.web.flow.SendTicketGrantingTicketAction;
import org.apereo.cas.web.flow.ServiceAuthorizationCheck;
import org.apereo.cas.web.flow.ServiceWarningAction;
import org.apereo.cas.web.flow.TerminateSessionAction;
import org.apereo.cas.web.flow.TicketGrantingTicketCheckAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.pac4j.core.config.Config;
import org.pac4j.springframework.web.ApplicationLogoutController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link CasSupportActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement
public class CasSupportActionsConfiguration {

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("argumentExtractors")
    private List argumentExtractors;

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
    
    @Bean
    public HandlerExceptionResolver errorHandlerResolver() {
        return new FlowExecutionExceptionResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "authenticationViaFormAction")
    public Action authenticationViaFormAction() {
        final AbstractAuthenticationAction a = new AbstractAuthenticationAction() {};
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
        a.setAdaptiveAuthenticationPolicy(this.adaptiveAuthenticationPolicy);
        return a;
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceAuthorizationCheck")
    public Action serviceAuthorizationCheck() {
        return new ServiceAuthorizationCheck(this.servicesManager);
    }

    @Bean
    @ConditionalOnMissingBean(name = "sendTicketGrantingTicketAction")
    public Action sendTicketGrantingTicketAction() {
        final SendTicketGrantingTicketAction bean = new SendTicketGrantingTicketAction();
        bean.setCreateSsoSessionCookieOnRenewAuthentications(casProperties.getSso().isRenewedAuthn());
        bean.setCentralAuthenticationService(centralAuthenticationService);
        bean.setServicesManager(servicesManager);
        bean.setTicketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator);
        bean.setAuthenticationSystemSupport(authenticationSystemSupport);
        return bean;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "logoutAction")
    public Action logoutAction() {
        final LogoutAction a = new LogoutAction();

        a.setFollowServiceRedirects(casProperties.getLogout().isFollowServiceRedirects());
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @Bean
    @ConditionalOnMissingBean(name = "initializeLoginAction")
    public Action initializeLoginAction() {
        final InitializeLoginAction a = new InitializeLoginAction();
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "initialFlowSetupAction")
    public Action initialFlowSetupAction() {
        final InitialFlowSetupAction bean = new InitialFlowSetupAction();
        bean.setArgumentExtractors(this.argumentExtractors);
        bean.setServicesManager(this.servicesManager);
        bean.setTicketGrantingTicketCookieGenerator(this.ticketGrantingTicketCookieGenerator);
        bean.setWarnCookieGenerator(this.warnCookieGenerator);
        return bean;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "initialAuthenticationRequestValidationAction")
    public Action initialAuthenticationRequestValidationAction() {
        final InitialAuthenticationRequestValidationAction a =
                new InitialAuthenticationRequestValidationAction();
        a.setRankedAuthenticationProviderWebflowEventResolver(rankedAuthenticationProviderWebflowEventResolver);
        return a;
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "genericSuccessViewAction")
    public Action genericSuccessViewAction() {
        final GenericSuccessViewAction a = new GenericSuccessViewAction(
                this.centralAuthenticationService, this.servicesManager, this.webApplicationServiceFactory);
        a.setRedirectUrl(casProperties.getView().getDefaultRedirectUrl());
        return a;
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateServiceTicketAction")
    public Action generateServiceTicketAction() {
        final GenerateServiceTicketAction a = new GenerateServiceTicketAction();
        a.setCentralAuthenticationService(this.centralAuthenticationService);
        a.setAuthenticationSystemSupport(this.authenticationSystemSupport);
        a.setTicketRegistrySupport(this.ticketRegistrySupport);
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @Bean
    @ConditionalOnMissingBean(name = "gatewayServicesManagementCheck")
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

    @Lazy
    @Autowired
    @Bean
    public Action terminateSessionAction(@Qualifier("config") final Config pac4jSecurityConfig) {
        final TerminateSessionAction a = new TerminateSessionAction();
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setTicketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator);
        a.setWarnCookieGenerator(warnCookieGenerator);
        
        final ApplicationLogoutController controller = new ApplicationLogoutController();
        controller.setConfig(pac4jSecurityConfig);
        a.setApplicationLogoutController(controller);
        return a;
    }

    @Bean
    @ConditionalOnMissingBean(name = "serviceWarningAction")
    public Action serviceWarningAction() {
        final ServiceWarningAction a = new ServiceWarningAction();
        a.setCentralAuthenticationService(this.centralAuthenticationService);
        a.setAuthenticationSystemSupport(this.authenticationSystemSupport);
        a.setTicketRegistrySupport(this.ticketRegistrySupport);
        a.setWarnCookieGenerator(this.warnCookieGenerator);
        return a;
    }
}
