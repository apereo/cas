package org.apereo.cas.ticket.registry;

import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.registry.support.LockingStrategy;

/**
 * This is {@link NoOpTicketRegistryCleaner} that simply disables support for ticket cleanup.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class NoOpTicketRegistryCleaner extends DefaultTicketRegistryCleaner {
    public NoOpTicketRegistryCleaner(final LockingStrategy lockingStrategy,
                                      final LogoutManager logoutManager,
                                      final TicketRegistry ticketRegistry,
                                      final boolean isCleanerEnabled) {
        super(lockingStrategy, logoutManager, ticketRegistry, isCleanerEnabled);
    }

    @Override
    protected boolean isCleanerSupported() {
        return false;
    }
}
