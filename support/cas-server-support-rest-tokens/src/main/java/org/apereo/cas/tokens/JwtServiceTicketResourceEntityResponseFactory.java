package org.apereo.cas.tokens;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.rest.factory.CasProtocolServiceTicketResourceEntityResponseFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.token.TokenTicketBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;

/**
 * This is {@link JwtServiceTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class JwtServiceTicketResourceEntityResponseFactory extends CasProtocolServiceTicketResourceEntityResponseFactory {
    /**
     * The ticket builder that produces tokens.
     */
    private final TokenTicketBuilder tokenTicketBuilder;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final ServicesManager servicesManager;

    public JwtServiceTicketResourceEntityResponseFactory(final CentralAuthenticationService centralAuthenticationService,
                                                         final TokenTicketBuilder tokenTicketBuilder,
                                                         final TicketRegistrySupport ticketRegistrySupport,
                                                         final ServicesManager servicesManager) {
        super(centralAuthenticationService);
        this.tokenTicketBuilder = tokenTicketBuilder;
        this.ticketRegistrySupport = ticketRegistrySupport;
        this.servicesManager = servicesManager;
    }

    @Override
    protected String grantServiceTicket(final String ticketGrantingTicket,
                                        final Service service,
                                        final AuthenticationResult authenticationResult) {
        val registeredService = this.servicesManager.findServiceBy(service);

        LOGGER.debug("Located registered service [{}] for [{}]", registeredService, service);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
        val tokenAsResponse = RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET.isAssignedTo(registeredService, BooleanUtils::toBoolean);

        if (!tokenAsResponse) {
            LOGGER.debug("Service [{}] does not require JWTs as tickets, given the properties assigned are [{}]", service, registeredService.getProperties());
            return super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        }

        val serviceTicket = super.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        val jwt = this.tokenTicketBuilder.build(serviceTicket, service);
        LOGGER.debug("Generated JWT [{}] for service [{}]", jwt, service);
        return jwt;
    }

    @Override
    public int getOrder() {
        return super.getOrder() - 1;
    }
}
