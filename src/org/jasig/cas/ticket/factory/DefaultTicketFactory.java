/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketCreatorNotFoundException;

/**
 * Factory to create new tickets. It works by allowing the user to register a list of ticket creators. The factory will attempt to match the class
 * requested with a <i>creator </i> and then delegate the work to that <i>creator </i>.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultTicketFactory implements TicketFactory {

    private List ticketCreators = new ArrayList();

    /**
     * @see org.jasig.cas.ticket.factory.TicketFactory#getTicket(java.lang.Class, org.jasig.cas.authentication.principal.Principal,
     * org.jasig.cas.ticket.CasAttributes, org.jasig.cas.ticket.Ticket)
     */
    public Ticket getTicket(final Class clazz, final Principal principal, final CasAttributes casAttributes, final Ticket parentTicket) {
        for (Iterator iter = this.ticketCreators.iterator(); iter.hasNext();) {
            TicketCreator ticketCreator = (TicketCreator)iter.next();

            if (ticketCreator.supports(clazz))
                return ticketCreator.createTicket(principal, casAttributes,
                    parentTicket);
        }

        throw new TicketCreatorNotFoundException("No TicketCreator registered for ticket type: " + clazz.getName());
    }

    /**
     * @param ticketCreators The ticketCreators to set.
     */
    public void setTicketCreators(List ticketCreators) {
        this.ticketCreators = ticketCreators;
    }
}
