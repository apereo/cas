/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

import java.util.List;

/**
 * Interface for returning the results of a successful validation of a ticket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface Assertion {

    /**
     * Get a List of Principals which represent the owners of the
     * GrantingTickets which granted the ticket that was validated. The first
     * Principal of this list is the Principal which originally authenticated to
     * CAS to obtain the first Granting Ticket. Subsequent Principals are those
     * associated with GrantingTickets that were granted from that original
     * granting ticket. The last Principal in this List is that associated with
     * the GrantingTicket that was the immediate grantor of the ticket that was
     * validated. The List returned by this method will contain at least one
     * Principal.
     * 
     * @return a List of Principals
     */
    List getChainedPrincipals();

    /**
     * True if the validated ticket was granted in the same transaction as that
     * in which its grantor GrantingTicket was originally issued.
     * 
     * @return true if validated ticket was granted simultaneous with its
     * grantor's issuance
     */
    boolean isFromNewLogin();

}