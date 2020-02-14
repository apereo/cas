package org.apereo.cas.web;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

/**
 * This is {@link ServiceValidateConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@Builder
public class ServiceValidateConfigurationContext {
    private final Set<CasProtocolValidationSpecification> validationSpecifications;

    private final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServicesManager servicesManager;

    private final CentralAuthenticationService centralAuthenticationService;

    private final ArgumentExtractor argumentExtractor;

    private final RequestedAuthenticationContextValidator<MultifactorAuthenticationProvider> requestedContextValidator;

    private final String authnContextAttribute;

    private final boolean renewEnabled;

    private final ServiceValidationViewFactory validationViewFactory;

    private ProxyHandler proxyHandler;
}
