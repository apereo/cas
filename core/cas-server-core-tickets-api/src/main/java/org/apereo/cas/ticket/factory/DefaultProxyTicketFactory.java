package org.apereo.cas.ticket.factory;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The {@link DefaultProxyTicketFactory} is responsible for
 * creating {@link ProxyTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultProxyTicketFactory implements ProxyTicketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProxyTicketFactory.class);

    /**
     * Default instance for the ticket id generator.
     */
    private final UniqueTicketIdGenerator defaultTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /**
     * Map to contain the mappings of service to {@link UniqueTicketIdGenerator}s.
     */
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /**
     * ExpirationPolicy for Service Tickets.
     */
    private final ExpirationPolicy proxyTicketExpirationPolicy;
    private final CipherExecutor<String, String> cipherExecutor;
    private final boolean onlyTrackMostRecentSession;

    public DefaultProxyTicketFactory(final ExpirationPolicy expirationPolicy, final Map<String, UniqueTicketIdGenerator> ticketIdGenerators,
                                     final CipherExecutor<String, String> cipherExecutor, final boolean onlyTrackMostRecentSession) {
        this.proxyTicketExpirationPolicy = expirationPolicy;
        this.uniqueTicketIdGeneratorsForService = ticketIdGenerators;
        this.cipherExecutor = cipherExecutor;
        this.onlyTrackMostRecentSession = onlyTrackMostRecentSession;
    }

    @Override
    public <T extends Ticket> T create(final ProxyGrantingTicket proxyGrantingTicket, final Service service) {
        final String ticketId = produceTicketIdentifier(service);
        return produceTicket(proxyGrantingTicket, service, ticketId);
    }

    /**
     * Produce ticket.
     *
     * @param <T>                 the type parameter
     * @param proxyGrantingTicket the proxy granting ticket
     * @param service             the service
     * @param ticketId            the ticket id
     * @return the ticket
     */
    protected <T extends Ticket> T produceTicket(final ProxyGrantingTicket proxyGrantingTicket, final Service service, final String ticketId) {
        final ProxyTicket serviceTicket = proxyGrantingTicket.grantProxyTicket(
                ticketId,
                service,
                this.proxyTicketExpirationPolicy,
                this.onlyTrackMostRecentSession);
        return (T) serviceTicket;
    }

    /**
     * Produce ticket identifier.
     *
     * @param service the service
     * @return the ticket id
     */
    protected String produceTicketIdentifier(final Service service) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        LOGGER.debug("Looking up ticket id generator for [{}]", uniqueTicketIdGenKey);
        UniqueTicketIdGenerator generator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (generator == null) {
            generator = this.defaultTicketIdGenerator;
            LOGGER.debug("Ticket id generator not found for [{}]. Using the default generator...", uniqueTicketIdGenKey);
        }

        String ticketId = generator.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode proxy ticket [{}]", ticketId);
            ticketId = this.cipherExecutor.encode(ticketId);
            LOGGER.debug("Encoded proxy ticket id [{}]", ticketId);
        }
        return ticketId;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
