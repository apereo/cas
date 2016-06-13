package org.apereo.cas.web.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.flow.AuthenticationViaFormAction;
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
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;

import javax.annotation.Resource;
import java.util.List;

/**
 * This is {@link CasSupportActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsConfiguration")
public class CasSupportActionsConfiguration {

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Resource(name = "servicesManager")
    private ServicesManager servicesManager;

    @Resource(name = "argumentExtractors")
    private List<ArgumentExtractor> argumentExtractors;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieRetrievingCookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;


    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

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
    public Action authenticationViaFormAction() {
        final AuthenticationViaFormAction a = new AuthenticationViaFormAction();
        a.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        a.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);
        return a;
    }

    @Bean
    public Action serviceAuthorizationCheck() {
        return new ServiceAuthorizationCheck(this.servicesManager);
    }

    @Bean
    public Action sendTicketGrantingTicketAction() {
        final SendTicketGrantingTicketAction bean = new SendTicketGrantingTicketAction();
        bean.setCreateSsoSessionCookieOnRenewAuthentications(casProperties.getSso().isRenewedAuthn());
        return bean;
    }

    @Bean
    public Action logoutAction() {
        final LogoutAction a = new LogoutAction();

        a.setFollowServiceRedirects(casProperties.getLogout().isFollowServiceRedirects());
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @Bean
    public Action initializeLoginAction() {
        final InitializeLoginAction a = new InitializeLoginAction();
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @Bean
    public Action initialFlowSetupAction() {
        final InitialFlowSetupAction bean = new InitialFlowSetupAction();
        bean.setEnableFlowOnAbsentServiceRequest(casProperties.getSso().isMissingService());
        bean.setArgumentExtractors(this.argumentExtractors);
        bean.setServicesManager(this.servicesManager);
        bean.setTicketGrantingTicketCookieGenerator(this.ticketGrantingTicketCookieGenerator);
        bean.setWarnCookieGenerator(this.warnCookieGenerator);
        bean.setGoogleAnalyticsTrackingId(casProperties.getGoogleAnalytics().getGoogleAnalyticsTrackingId());
        bean.setTrackGeoLocation(casProperties.getEvents().isTrackGeolocation());
        return bean;
    }

    @Bean
    public Action initialAuthenticationRequestValidationAction() {
        final InitialAuthenticationRequestValidationAction a =
                new InitialAuthenticationRequestValidationAction();
        a.setRankedAuthenticationProviderWebflowEventResolver(rankedAuthenticationProviderWebflowEventResolver);
        return a;
    }

    @Bean
    public Action genericSuccessViewAction() {
        return new GenericSuccessViewAction(this.centralAuthenticationService);
    }

    @Bean
    public Action generateServiceTicketAction() {
        final GenerateServiceTicketAction a = new GenerateServiceTicketAction();
        a.setCentralAuthenticationService(this.centralAuthenticationService);
        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setTicketRegistrySupport(ticketRegistrySupport);
        return a;
    }

    @Bean
    public Action gatewayServicesManagementCheck() {
        return new GatewayServicesManagementCheck();
    }

    @Bean
    public Action frontChannelLogoutAction() {
        return new FrontChannelLogoutAction();
    }

    @Bean
    public Action ticketGrantingTicketCheckAction() {
        return new TicketGrantingTicketCheckAction(this.centralAuthenticationService);
    }

    @Bean
    public Action terminateSessionAction() {
        final TerminateSessionAction a = new TerminateSessionAction();
        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setTicketGrantingTicketCookieGenerator(ticketGrantingTicketCookieGenerator);
        a.setWarnCookieGenerator(warnCookieGenerator);
        return a;
    }

    @Bean
    public Action serviceWarningAction() {
        return new ServiceWarningAction();
    }
}
