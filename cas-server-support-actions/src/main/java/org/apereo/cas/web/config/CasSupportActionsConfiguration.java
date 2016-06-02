package org.apereo.cas.web.config;

import org.apereo.cas.services.ServicesManager;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link CasSupportActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casSupportActionsConfiguration")
public class CasSupportActionsConfiguration {
    
    @Bean
    public HandlerExceptionResolver errorHandlerResolver() {
        return new FlowExecutionExceptionResolver();
    }
    
    @Bean
    public Action authenticationViaFormAction() {
        return new AuthenticationViaFormAction();
    }

    @Bean
    @Autowired
    public Action serviceAuthorizationCheck(final ServicesManager servicesManager) {
        return new ServiceAuthorizationCheck(servicesManager);
    }

    @Bean
    public Action sendTicketGrantingTicketAction() {
        return new SendTicketGrantingTicketAction();
    }

    @Bean
    public Action logoutAction() {
        return new LogoutAction();
    }

    @Bean
    public Action initializeLoginAction() {
        return new InitializeLoginAction();
    }

    @Bean
    public Action initialFlowSetupAction() {
        return new InitialFlowSetupAction();
    }

    @Bean
    public Action initialAuthenticationRequestValidationAction() {
        return new InitialAuthenticationRequestValidationAction();
    }

    @Bean
    public Action genericSuccessViewAction() {
        return new GenericSuccessViewAction();
    }

    @Bean
    public Action generateServiceTicketAction() {
        return new GenerateServiceTicketAction();
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
        return new TicketGrantingTicketCheckAction();
    }

    @Bean
    public Action terminateSessionAction() {
        return new TerminateSessionAction();
    }

    @Bean
    public Action serviceWarningAction() {
        return new ServiceWarningAction();
    }
}
