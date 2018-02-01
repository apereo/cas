package org.apereo.cas.ticket.factory;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import java.util.Map;

/**
 * The {@link DefaultServiceTicketFactory} is responsible for
 * creating {@link ServiceTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class DefaultServiceTicketFactory implements ServiceTicketFactory {



    /**
     * The Cipher executor.
     */
    protected CipherExecutor<String, String> cipherExecutor;

    private final UniqueTicketIdGenerator defaultServiceTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;
    private final ExpirationPolicy serviceTicketExpirationPolicy;

    private boolean trackMostRecentSession = true;

    public DefaultServiceTicketFactory(final ExpirationPolicy serviceTicketExpirationPolicy,
                                       final Map<String, UniqueTicketIdGenerator> ticketIdGeneratorMap,
                                       final boolean onlyTrackMostRecentSession, final CipherExecutor cipherExecutor) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
        this.uniqueTicketIdGeneratorsForService = ticketIdGeneratorMap;
        this.trackMostRecentSession = onlyTrackMostRecentSession;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public <T extends Ticket> T create(final TicketGrantingTicket ticketGrantingTicket, final Service service,
                                       final boolean credentialProvided, final Class<T> clazz) {
        String ticketId = produceTicketIdentifier(service, ticketGrantingTicket, credentialProvided);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode service ticket [{}]", ticketId);
            ticketId = this.cipherExecutor.encode(ticketId);
            LOGGER.debug("Encoded service ticket id [{}]", ticketId);
        }
        return produceTicket(ticketGrantingTicket, service, credentialProvided, ticketId, clazz);
    }

    /**
     * Produce ticket.
     *
     * @param <T>                  the type parameter
     * @param ticketGrantingTicket the ticket granting ticket
     * @param service              the service
     * @param credentialProvided   the credential provided
     * @param ticketId             the ticket id
     * @param clazz                the clazz
     * @return the ticket
     */
    protected <T extends Ticket> T produceTicket(final TicketGrantingTicket ticketGrantingTicket, final Service service,
                                                 final boolean credentialProvided, final String ticketId, final Class<T> clazz) {
        final ServiceTicket result = ticketGrantingTicket.grantServiceTicket(
                ticketId,
                service,
                this.serviceTicketExpirationPolicy,
                credentialProvided,
                trackMostRecentSession);

        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Result [" + result
                + " is of type " + result.getClass()
                + " when we were expecting " + clazz);
        }
        return (T) result;
    }

    /**
     * Produce ticket identifier.
     *
     * @param service              the service
     * @param ticketGrantingTicket the ticket granting ticket
     * @param credentialProvided   whether credentials where directly provided
     * @return the tI don't knowet id
     */
    protected String produceTicketIdentifier(final Service service, final TicketGrantingTicket ticketGrantingTicket,
                                             final boolean credentialProvided) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator = null;
        if (this.uniqueTicketIdGeneratorsForService != null && !this.uniqueTicketIdGeneratorsForService.isEmpty()) {
            LOGGER.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
            serviceTicketUniqueTicketIdGenerator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        }
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = this.defaultServiceTicketIdGenerator;
            LOGGER.debug("Service ticket id generator not found for [{}]. Using the default generator...",
                    uniqueTicketIdGenKey);
        }

        return serviceTicketUniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
