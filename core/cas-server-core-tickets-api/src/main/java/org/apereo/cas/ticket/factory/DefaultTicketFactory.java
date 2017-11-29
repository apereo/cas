package org.apereo.cas.ticket.factory;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.ServiceTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link DefaultTicketFactory} is responsible for creating ticket factory objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultTicketFactory implements TicketFactory {

    private final Map<String, Object> factoryMap;

    public DefaultTicketFactory(final ProxyGrantingTicketFactory pgtFactory, final TicketGrantingTicketFactory tgtFactory, final ServiceTicketFactory stFactory,
                                final ProxyTicketFactory proxyTicketFactory) {

        Assert.notNull(tgtFactory, "ticketGrantingTicketFactory cannot be null");
        Assert.notNull(pgtFactory, "proxyGrantingTicketFactory cannot be null");
        Assert.notNull(stFactory, "serviceTicketFactory cannot be null");
        Assert.notNull(proxyTicketFactory, "proxyTicketFactory cannot be null");

        this.factoryMap = new HashMap<>();

        this.factoryMap.put(ProxyGrantingTicket.class.getCanonicalName(), pgtFactory);
        this.factoryMap.put(TicketGrantingTicket.class.getCanonicalName(), tgtFactory);
        this.factoryMap.put(ServiceTicket.class.getCanonicalName(), stFactory);
        this.factoryMap.put(ProxyTicket.class.getCanonicalName(), proxyTicketFactory);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this.factoryMap.get(clazz.getCanonicalName());
    }
}
