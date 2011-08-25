/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Principal;


public interface LdapPwdCentralAuthenticationService extends CentralAuthenticationService {
    
    /**
     * Return a principal for a TicketGrantingTicket
     * 
     * @param id The String identifier of the TicketGrantingTicket
     * @return Principal The principal used when the TicketGrantingTicket was created
     */
    public Principal getPrincipal(String id);
}
