package org.apereo.cas.ws.idp.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.ws.idp.services.WSFederationRelyingPartyTokenProducer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.jasig.cas.client.validation.TicketValidator;

/**
 * This is {@link WSFederationRequestConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class WSFederationRequestConfigurationContext {
    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final Service callbackService;

    private final CasConfigurationProperties casProperties;

    private final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    private final HttpClient httpClient;

    private final TicketFactory ticketFactory;

    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final WSFederationRelyingPartyTokenProducer relyingPartyTokenProducer;

    private final TicketValidator ticketValidator;

    private final SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher;
}
