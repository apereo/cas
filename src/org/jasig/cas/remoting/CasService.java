/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting;

import org.jasig.cas.authentication.AuthenticationRequest;

/**
 * Interface for the services exposed by CAS remote services. These services can be either SOAP, RMI, etc. The CasService does not care.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface CasService {
    
    String getTicketGrantingTicket(final AuthenticationRequest request);
    
    String getServiceTicket(final String ticketGrantingTicketId, final String service);
}
