package org.apereo.cas.ticket.tracking;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.concurrent.CasReentrantLock;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link AllServicesSessionTrackingPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class AllServicesSessionTrackingPolicy implements TicketTrackingPolicy {
    protected final TicketRegistry ticketRegistry;

    private final CasReentrantLock lock = new CasReentrantLock();


    @Override
    public void trackTicket(final TicketGrantingTicket ownerTicket, final Ticket ticket) {
        val serviceTicket = (ServiceTicket) ticket;
        lock.tryLock(__ -> {
            ownerTicket.update();
            serviceTicket.getService().setPrincipal(ownerTicket.getRoot().getAuthentication().getPrincipal().getId());
            beforeTrackingServiceTicket(ownerTicket, serviceTicket);
            ownerTicket.getServices().put(serviceTicket.getId(), serviceTicket.getService());
        });
    }

    protected void beforeTrackingServiceTicket(final TicketGrantingTicket ownerTicket,
                                               final ServiceTicket serviceTicket) {
    }
}
