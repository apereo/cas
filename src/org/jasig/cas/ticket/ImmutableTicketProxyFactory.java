/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Factory strategy that is responsible for creating "immutable view" of the Ticket and its sub-types by wrapping the internal implementation of a
 * Ticket in the "protective proxy".
 * <p>
 * Typical implementation might for example use JDK 1.3+ dynamic proxy or SpringAOP proxy built on top of JDK dynamic proxy.
 * <p>
 * Note: this is a SPI interface used internally by <code>TicketManager</code>
 * 
 * @author Dmitriy Kopylenko
 * @version $Id$
 * @see org.jasig.cas.ticket.Ticket
 * @see org.jasig.cas.ticket.TicketManager
 */
public interface ImmutableTicketProxyFactory {

    /**
     * Get protective proxy for a given Ticket implementation.
     */
    Ticket getProxyForTicket(Ticket ticketImpl);
}