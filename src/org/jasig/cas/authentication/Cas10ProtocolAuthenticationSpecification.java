/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;


/**
 * A mapping of the Cas 1.0 protocol for authentication.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Cas10ProtocolAuthenticationSpecification implements AuthenticationSpecification {
    private boolean renew;
    
    public Cas10ProtocolAuthenticationSpecification() {
        renew = false;
    }
    
    public Cas10ProtocolAuthenticationSpecification(boolean renew) {
        this.renew = renew;
    }
    
    public void setRenew(boolean renew) {
        this.renew = renew;
    }
    
    public boolean isRenew() {
        return renew;
    }
    /**
     * @see org.jasig.cas.authentication.AuthenticationSpecification#isSatisfiedBy(org.jasig.cas.ticket.Ticket)
     */
    public boolean isSatisfiedBy(Ticket ticket) {
        if (!ServiceTicket.class.isAssignableFrom(ticket.getClass()))
            return false;
        
        ServiceTicket serviceTicket = (ServiceTicket) ticket;
        
        if (!this.renew)
            return true;

        return serviceTicket.isFromNewLogin() && this.renew;
    }

}
