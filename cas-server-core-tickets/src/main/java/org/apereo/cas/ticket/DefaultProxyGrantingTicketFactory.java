package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * The {@link DefaultTicketGrantingTicketFactory} is responsible
 * for creating {@link ProxyGrantingTicket} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RefreshScope
@Component("defaultProxyGrantingTicketFactory")
public class DefaultProxyGrantingTicketFactory implements ProxyGrantingTicketFactory {

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Used to generate ids for {@link TicketGrantingTicket}s
     * created.
     */
    
    @Resource(name="ticketGrantingTicketUniqueIdGenerator")
    protected UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator;

    /** Expiration policy for ticket granting tickets. */
    
    @Resource(name="grantingTicketExpirationPolicy")
    protected ExpirationPolicy ticketGrantingTicketExpirationPolicy;

    @Override
    public <T extends ProxyGrantingTicket> T create(final ServiceTicket serviceTicket,
                                                    final Authentication authentication)
                                                    throws AbstractTicketException {
        final String pgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(
                ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX);
        final ProxyGrantingTicket proxyGrantingTicket = serviceTicket.grantProxyGrantingTicket(pgtId,
                authentication, this.ticketGrantingTicketExpirationPolicy);
        return (T) proxyGrantingTicket;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    public void setTicketGrantingTicketUniqueTicketIdGenerator(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator) {
        this.ticketGrantingTicketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
    }

    public void setTicketGrantingTicketExpirationPolicy(final ExpirationPolicy ticketGrantingTicketExpirationPolicy) {
        this.ticketGrantingTicketExpirationPolicy = ticketGrantingTicketExpirationPolicy;
    }
}
