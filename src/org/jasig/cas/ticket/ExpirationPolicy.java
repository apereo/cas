/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Strategy that determines if the ticket is expired.
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.AbstractTicket
 */
public interface ExpirationPolicy {

    public boolean isExpired(AbstractTicket ticket);
}
