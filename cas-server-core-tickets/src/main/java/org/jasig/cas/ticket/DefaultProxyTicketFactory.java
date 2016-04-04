package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.ticket.proxy.ProxyTicketFactory;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * The {@link DefaultProxyTicketFactory} is responsible for
 * creating {@link ProxyTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("defaultProxyTicketFactory")
public class DefaultProxyTicketFactory implements ProxyTicketFactory {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Default instance for the ticket id generator. */
    
    protected UniqueTicketIdGenerator defaultTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** Map to contain the mappings of service to {@link UniqueTicketIdGenerator}s. */
    
    @Resource(name="uniqueIdGeneratorsMap")
    protected Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;

    /** Whether we should track the most recent session by keeping the latest service ticket. */
    @Value("${tgt.track.recent.session:true}")
    protected boolean onlyTrackMostRecentSession = true;

    /** ExpirationPolicy for Service Tickets. */
    
    @Resource(name="proxyTicketExpirationPolicy")
    protected ExpirationPolicy proxyTicketExpirationPolicy;

    @Override
    public <T extends Ticket> T create(final ProxyGrantingTicket proxyGrantingTicket,
                                       final Service service) {
        final String uniqueTicketIdGenKey = service.getClass().getName();
        logger.debug("Looking up ticket id generator for [{}]", uniqueTicketIdGenKey);
        UniqueTicketIdGenerator generator = this.uniqueTicketIdGeneratorsForService.get(uniqueTicketIdGenKey);
        if (generator == null) {
            generator = this.defaultTicketIdGenerator;
            logger.debug("Ticket id generator not found for [{}]. Using the default generator...",
                    uniqueTicketIdGenKey);
        }

        final String ticketId = generator.getNewTicketId(ProxyTicket.PROXY_TICKET_PREFIX);
        final ProxyTicket serviceTicket = proxyGrantingTicket.grantProxyTicket(
                ticketId,
                service,
                this.proxyTicketExpirationPolicy,
                this.onlyTrackMostRecentSession);
        return (T) serviceTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public boolean isOnlyTrackMostRecentSession() {
        return this.onlyTrackMostRecentSession;
    }

    public void setOnlyTrackMostRecentSession(final boolean onlyTrackMostRecentSession) {
        this.onlyTrackMostRecentSession = onlyTrackMostRecentSession;
    }

    public void setUniqueTicketIdGeneratorsForService(final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService) {
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
    }

    public void setProxyTicketExpirationPolicy(final ExpirationPolicy proxyTicketExpirationPolicy) {
        this.proxyTicketExpirationPolicy = proxyTicketExpirationPolicy;
    }
}
