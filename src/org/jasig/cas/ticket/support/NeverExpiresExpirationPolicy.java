/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.ticket.AbstractTicket;
import org.jasig.cas.ticket.ExpirationPolicy;

/**
 * NeverExpiresExpirationPolicy always answers false when asked if a Ticket is expired. Use this policy when you want a
 * Ticket to live forever, or at least as long as the particular CAS Universe exists.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class NeverExpiresExpirationPolicy implements ExpirationPolicy {

    /**
     * @see org.jasig.cas.ticket.ExpirationPolicy#isExpired(org.jasig.cas.domain.support.AbstractTicket)
     */
    public boolean isExpired(final AbstractTicket ticket) {
        return false;
    }
}