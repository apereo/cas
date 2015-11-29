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

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, TicketFactory> factoryMap;

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

        if (this.ticketGrantingTicketFactory == null) {
            throw new RuntimeException("ticketGrantingTicketFactory cannot be null");
        }
        if (this.proxyGrantingTicketFactory == null) {
            throw new RuntimeException("proxyGrantingTicketFactory cannot be null");
        }
        if (this.serviceTicketFactory == null) {
            throw new RuntimeException("serviceTicketFactory cannot be null");
        }
        if (this.proxyTicketFactory == null) {
            throw new RuntimeException("proxyTicketFactory cannot be null");
        }

        this.factoryMap.put(ProxyGrantingTicket.class.getCanonicalName(), this.proxyGrantingTicketFactory);
        this.factoryMap.put(TicketGrantingTicket.class.getCanonicalName(), this.ticketGrantingTicketFactory);
        this.factoryMap.put(ServiceTicket.class.getCanonicalName(), this.serviceTicketFactory);
        this.factoryMap.put(ProxyTicket.class.getCanonicalName(), this.proxyTicketFactory);
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        final TicketFactory factory = this.factoryMap.get(clazz.getCanonicalName());
        return (T) factory;
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
