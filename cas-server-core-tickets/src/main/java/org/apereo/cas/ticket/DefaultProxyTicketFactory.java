package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;

/**
 * The {@link DefaultProxyTicketFactory} is responsible for
 * creating {@link ProxyTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultProxyTicketFactory implements ProxyTicketFactory {
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Default instance for the ticket id generator.
     */
    protected UniqueTicketIdGenerator defaultTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /**
     * Map to contain the mappings of service to {@link UniqueTicketIdGenerator}s.
     */
    @Resource(name = "uniqueIdGeneratorsMap")
    protected Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService;
        
    /**
     * ExpirationPolicy for Service Tickets.
     */
    @Resource(name = "proxyTicketExpirationPolicy")
    protected ExpirationPolicy proxyTicketExpirationPolicy;

    @Autowired
    private CasConfigurationProperties casProperties;
    
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
                casProperties.getTgt().isOnlyTrackMostRecentSession());
        return (T) serviceTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }
    
    public void setUniqueTicketIdGeneratorsForService(final Map<String, UniqueTicketIdGenerator> uniqueTicketIdGeneratorsForService) {
        this.uniqueTicketIdGeneratorsForService = uniqueTicketIdGeneratorsForService;
    }

    public void setProxyTicketExpirationPolicy(final ExpirationPolicy proxyTicketExpirationPolicy) {
        this.proxyTicketExpirationPolicy = proxyTicketExpirationPolicy;
    }
}
