package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.ticket.proxy.ProxyTicketFactory;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The {@link DefaultProxyTicketFactory} is responsible for
 * creating {@link ProxyTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("defaultProxyTicketFactory")
public class DefaultProxyTicketFactory implements ProxyTicketFactory {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    @NotNull
    protected final UniqueTicketIdGenerator defaultServiceTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** Map to contain the mappings of service to {@link UniqueTicketIdGenerator}s. */
    @NotNull
    @Resource(name="uniqueIdGeneratorsMap")
    protected Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Whether we should track the most recent session by keeping the latest service ticket. */
    @Value("${tgt.onlyTrackMostRecentSession:true}")
    protected boolean onlyTrackMostRecentSession = true;

    /** ExpirationPolicy for Service Tickets. */
    @NotNull
    @Resource(name="serviceTicketExpirationPolicy")
    protected ExpirationPolicy serviceTicketExpirationPolicy;

    @Override
    public <T extends Ticket> T create(final ProxyGrantingTicket proxyGrantingTicket,
                                       final Service service) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        logger.debug("Looking up service ticket id generator for [{}]", uniqueTicketIdGenKey);
        UniqueTicketIdGenerator serviceTicketUniqueTicketIdGenerator =
                this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (serviceTicketUniqueTicketIdGenerator == null) {
            serviceTicketUniqueTicketIdGenerator = this.defaultServiceTicketIdGenerator;
            logger.debug("Service ticket id generator not found for [{}]. Using the default generator...",
                    uniqueTicketIdGenKey);
        }

        final String ticketId = serviceTicketUniqueTicketIdGenerator.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX);
        final ProxyTicket serviceTicket = proxyGrantingTicket.grantProxyTicket(
                ticketId,
                service,
                this.serviceTicketExpirationPolicy,
                this.onlyTrackMostRecentSession);
        return (T) serviceTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public final boolean isOnlyTrackMostRecentSession() {
        return onlyTrackMostRecentSession;
    }

    public final void setOnlyTrackMostRecentSession(final boolean onlyTrackMostRecentSession) {
        this.onlyTrackMostRecentSession = onlyTrackMostRecentSession;
    }

    public void setUniqueTicketIdGeneratorsForService(final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService) {
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
    }

    public void setServiceTicketExpirationPolicy(final ExpirationPolicy serviceTicketExpirationPolicy) {
        this.serviceTicketExpirationPolicy = serviceTicketExpirationPolicy;
    }
}
