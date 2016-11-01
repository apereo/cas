package org.apereo.cas.ticket;

import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.proxy.ProxyTicketFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link DefaultTicketFactory} is responsible for creating ticket factory objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultTicketFactory implements TicketFactory {

    private Map<String, Object> factoryMap;

    private ProxyTicketFactory proxyTicketFactory;
    
    private ServiceTicketFactory serviceTicketFactory;

    private TicketGrantingTicketFactory ticketGrantingTicketFactory;

    private ProxyGrantingTicketFactory proxyGrantingTicketFactory;

    /**
     * Initialize.
     */
    @PostConstruct
    public void initialize() {
        this.factoryMap = new HashMap<>();

        validateFactoryInstances();

        this.factoryMap.put(ProxyGrantingTicket.class.getCanonicalName(), this.proxyGrantingTicketFactory);
        this.factoryMap.put(TicketGrantingTicket.class.getCanonicalName(), this.ticketGrantingTicketFactory);
        this.factoryMap.put(ServiceTicket.class.getCanonicalName(), this.serviceTicketFactory);
        this.factoryMap.put(ProxyTicket.class.getCanonicalName(), this.proxyTicketFactory);
    }

    private void validateFactoryInstances() {
        Assert.notNull(this.ticketGrantingTicketFactory, "ticketGrantingTicketFactory cannot be null");
        Assert.notNull(this.proxyGrantingTicketFactory, "proxyGrantingTicketFactory cannot be null");
        Assert.notNull(this.serviceTicketFactory, "serviceTicketFactory cannot be null");
        Assert.notNull(this.proxyTicketFactory, "proxyTicketFactory cannot be null");
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        validateFactoryInstances();

        return (T) this.factoryMap.get(clazz.getCanonicalName());
    }

    public void setTicketGrantingTicketFactory(final TicketGrantingTicketFactory ticketGrantingTicketFactory) {
        this.ticketGrantingTicketFactory = ticketGrantingTicketFactory;
    }

    public void setProxyGrantingTicketFactory(final ProxyGrantingTicketFactory proxyGrantingTicketFactory) {
        this.proxyGrantingTicketFactory = proxyGrantingTicketFactory;
    }

    public void setServiceTicketFactory(final ServiceTicketFactory serviceTicketFactory) {
        this.serviceTicketFactory = serviceTicketFactory;
    }

    public void setProxyTicketFactory(final ProxyTicketFactory proxyTicketFactory) {
        this.proxyTicketFactory = proxyTicketFactory;
    }
}
