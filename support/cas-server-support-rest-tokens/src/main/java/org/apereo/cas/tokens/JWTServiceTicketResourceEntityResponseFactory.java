package org.apereo.cas.tokens;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.factory.DefaultServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.TokenTicketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link JWTServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTServiceTicketResourceEntityResponseFactory extends DefaultServiceTicketResourceEntityResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTServiceTicketResourceEntityResponseFactory.class);

    /**
     * The ticket builder that produces tokens.
     */
    private final TokenTicketBuilder tokenTicketBuilder;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final ServicesManager servicesManager;

    public JWTServiceTicketResourceEntityResponseFactory(final CentralAuthenticationService centralAuthenticationService,
                                                         final TokenTicketBuilder tokenTicketBuilder,
                                                         final TicketRegistrySupport ticketRegistrySupport,
                                                         final ServicesManager servicesManager) {
        super(centralAuthenticationService);
        this.tokenTicketBuilder = tokenTicketBuilder;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.servicesManager = servicesManager;
    }

    @Override
    protected String grantServiceTicket(final String ticketGrantingTicket, final Service service,
                                        final AuthenticationResult authenticationResult) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        LOGGER.debug("Located registered service [{}] for [{}]", registeredService, service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        final boolean tokenAsResponse = RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_RESPONSE.isAssignedTo(registeredService)
                || RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.isAssignedTo(registeredService);
        
        if (!tokenAsResponse) {
            LOGGER.debug("Service [{}] does not require JWTs as tickets", service);
            return super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        }

        final String serviceTicket = super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        final String jwt = this.tokenTicketBuilder.build(serviceTicket, service);
        LOGGER.debug("Generated JWT [{}] for service [{}]", jwt, service);
        return jwt;
    }
}
