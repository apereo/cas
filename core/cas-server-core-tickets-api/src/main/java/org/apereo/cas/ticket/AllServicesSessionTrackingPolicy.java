package org.apereo.cas.ticket;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import lombok.RequiredArgsConstructor;

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
    public void trackServiceTicket(final AuthenticatedServicesAwareTicketGrantingTicket ownerTicket,
                                   final ServiceTicket serviceTicket) {
        lock.tryLock(__ -> {
            ownerTicket.update();
            serviceTicket.getService().setPrincipal(ownerTicket.getRoot().getAuthentication().getPrincipal().getId());
            beforeTrackingServiceTicket(ownerTicket, serviceTicket);
            ownerTicket.getServices().put(serviceTicket.getId(), serviceTicket.getService());
        });
    }

    protected void beforeTrackingServiceTicket(final AuthenticatedServicesAwareTicketGrantingTicket ownerTicket,
                                               final ServiceTicket serviceTicket) {
    }
}
