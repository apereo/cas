package org.jasig.cas.ticket;

import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicketFactory;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.ticket.proxy.ProxyTicketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
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
@Component("defaultTicketFactory")
public class DefaultTicketFactory implements TicketFactory {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, Object> factoryMap;

    @Autowired
    @Qualifier("defaultProxyTicketFactory")
    private ProxyTicketFactory proxyTicketFactory;

    @Autowired
    @Qualifier("defaultServiceTicketFactory")
    private ServiceTicketFactory serviceTicketFactory;

    @Autowired
    @Qualifier("defaultTicketGrantingTicketFactory")
    private TicketGrantingTicketFactory ticketGrantingTicketFactory;

    @Autowired
    @Qualifier("defaultProxyGrantingTicketFactory")
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
