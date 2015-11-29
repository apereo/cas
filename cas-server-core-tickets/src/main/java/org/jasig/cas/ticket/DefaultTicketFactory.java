package org.jasig.cas.ticket;

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
        this.factoryMap.put(ProxyGrantingTicket.class.getCanonicalName(), this.proxyGrantingTicketFactory);
        this.factoryMap.put(TicketGrantingTicket.class.getCanonicalName(), this.ticketGrantingTicketFactory);

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
}
