package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.proxy.ProxyHandler;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.validation.CasProtocolValidationSpecification;
import org.apereo.cas.validation.RequestedAuthenticationContextValidator;
import org.apereo.cas.validation.ServiceTicketValidationAuthorizersExecutionPlan;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link ServiceValidateConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
@With
@AllArgsConstructor
public class ServiceValidateConfigurationContext {
    private final Set<CasProtocolValidationSpecification> validationSpecifications;

    private final ServiceTicketValidationAuthorizersExecutionPlan validationAuthorizers;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServicesManager servicesManager;

    private final CentralAuthenticationService centralAuthenticationService;

    private final ArgumentExtractor argumentExtractor;

    private final RequestedAuthenticationContextValidator requestedContextValidator;

    private final CasConfigurationProperties casProperties;

    private final ServiceValidationViewFactory validationViewFactory;

    private final TicketRegistry ticketRegistry;

    private final ServiceFactory serviceFactory;

    private final PrincipalFactory principalFactory;

    private final PrincipalResolver principalResolver;

    @NonNull
    private final ConfigurableApplicationContext applicationContext;

    private ProxyHandler proxyHandler;
}
