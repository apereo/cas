/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Domain object to model a ticket granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TicketGrantingTicketImpl extends AbstractTicket implements TicketGrantingTicket {

    private static final long serialVersionUID = -8673232562725683059L;

    public TicketGrantingTicketImpl(final String id, final Principal person, final ExpirationPolicy policy) {
        super(id, person, policy);
    }
}
