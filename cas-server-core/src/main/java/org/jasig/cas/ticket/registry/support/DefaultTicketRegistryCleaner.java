/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.inspektr.common.ioc.annotation.NotNull;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class that will iterate through the registry and check for tickets that are
 * expired. If a ticket is expired it is removed from the registry. This process
 * is only required so that the size of the TicketRegistry will not grow
 * significantly large. The functionality of CAS is not dependent on a Ticket
 * being removed as soon as it is expired.
 * <p>
 * Note that this version grabs an Unmodifiable collection and does the
 * expiration checking outside of the synchronization block, thus allowing
 * processing to continue.
 * </p>
 * <p>
 * The following property is required.
 * </p>
 * <ul>
 * <li>ticketRegistry - the Registry to clean.</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultTicketRegistryCleaner implements RegistryCleaner {

    /** The Commons Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** The instance of the TicketRegistry to clean. */
    @NotNull
    private TicketRegistry ticketRegistry;

    public void clean() {
        final List<Ticket> ticketsToRemove = new ArrayList<Ticket>();
        final Collection<Ticket> ticketsInCache;

        log
            .info("Starting cleaning of expired tickets from ticket registry at ["
                + new Date() + "]");

        ticketsInCache = this.ticketRegistry.getTickets();

        for (final Ticket ticket : ticketsInCache) {
            if (ticket.isExpired()) {
                ticketsToRemove.add(ticket);
            }
        }

        log.info(ticketsToRemove.size()
            + " found to be removed.  Removing now.");

        for (final Ticket ticket : ticketsToRemove) {
            this.ticketRegistry.deleteTicket(ticket.getId());
        }

        log
            .info("Finished cleaning of expired tickets from ticket registry at ["
                + new Date() + "]");
    }

    /**
     * @param ticketRegistry The ticketRegistry to set.
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
