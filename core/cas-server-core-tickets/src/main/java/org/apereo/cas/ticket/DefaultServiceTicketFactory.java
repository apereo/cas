package org.apereo.cas.ticket;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The {@link DefaultServiceTicketFactory} is responsible for
 * creating {@link ServiceTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultServiceTicketFactory implements ServiceTicketFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceTicketFactory.class);

    /** Default instance for the ticket id generator. */
    private final UniqueTicketIdGenerator defaultServiceTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
    
    /** Map to contain the mappings of service to {@link UniqueTicketIdGenerator}s. */
    private final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;
    
    /** ExpirationPolicy for Service Tickets. */
    private final ExpirationPolicy serviceTicketExpirationPolicy;
    private final CipherExecutor<String, String> cipherExecutor;
    private boolean trackMostRecentSession = true;

    public DefaultServiceTicketFactory(final ExpirationPolicy serviceTicketExpirationPolicy, final Map<String, UniqueTicketIdGenerator> ticketIdGeneratorMap,
                                       final boolean onlyTrackMostRecentSession, final CipherExecutor cipherExecutor) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
        this.uniqueTicketIdGeneratorsForService = ticketIdGeneratorMap;
        this.trackMostRecentSession = onlyTrackMostRecentSession;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public <T extends Ticket> T create(final TicketGrantingTicket ticketGrantingTicket, final Service service, final boolean credentialProvided) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator = null;
        if (this.uniqueTicketIdGeneratorsForService != null && !this.uniqueTicketIdGeneratorsForService.isEmpty()) {
            LOGGER.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
            serviceTicketUniqueTicketIdGenerator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        }
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = this.defaultServiceTicketIdGenerator;
            LOGGER.debug("Service ticket id generator not found for [{}]. Using the default generator...", uniqueTicketIdGenKey);
        }

        String ticketId = serviceTicketUniqueTicketIdGenerator.getNewTicketId(ServiceTicket.PREFIX);
        if (this.cipherExecutor != null) {
            LOGGER.debug("Attempting to encode service ticket {}", ticketId);
            ticketId = this.cipherExecutor.encode(ticketId);
            LOGGER.debug("Encoded service ticket id {}", ticketId);
        }
        
        final ServiceTicket serviceTicket = ticketGrantingTicket.grantServiceTicket(
                ticketId,
                service,
                this.serviceTicketExpirationPolicy,
                credentialProvided,
                trackMostRecentSession);
        return (T) serviceTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
}
