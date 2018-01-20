package org.apereo.cas.ticket.factory;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link DefaultTicketFactory} is responsible for creating ticket factory objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class DefaultTicketFactory implements TicketFactory {

    private final Map<String, Object> factoryMap;

    public DefaultTicketFactory(@NonNull final ProxyGrantingTicketFactory pgtFactory, @NonNull final TicketGrantingTicketFactory tgtFactory,
                                @NonNull final ServiceTicketFactory stFactory,
                                @NonNull final ProxyTicketFactory proxyTicketFactory) {

        this.factoryMap = new HashMap<>();
        this.factoryMap.put(ProxyGrantingTicket.class.getCanonicalName(), pgtFactory);
        this.factoryMap.put(TicketGrantingTicket.class.getCanonicalName(), tgtFactory);
        this.factoryMap.put(ServiceTicket.class.getCanonicalName(), stFactory);
        this.factoryMap.put(ProxyTicket.class.getCanonicalName(), proxyTicketFactory);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return (TicketFactory) this.factoryMap.get(clazz.getCanonicalName());
    }
}
