/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import org.jasig.cas.authentication.AuthenticationRequest;
import org.jasig.cas.remoting.CasService;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

/**
 * Actual implementation for JAX-RPC that delegates to the actual CasService.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class JaxRpcCasService extends ServletEndpointSupport implements CasService, RemoteCasService {

    private static final String CONST_CAS_SERVICE_NAME = "casService";

    private CasService casService;

    protected void onInit() {
        this.casService = (CasService)getWebApplicationContext().getBean(CONST_CAS_SERVICE_NAME);
    }

    /**
     * @see org.jasig.cas.remoting.CasService#getServiceTicket(java.lang.String, java.lang.String)
     */
    public String getServiceTicket(final String ticketGrantingTicketId, final String service) {
        return this.casService.getServiceTicket(ticketGrantingTicketId, service);
    }
    /**
     * @see org.jasig.cas.remoting.CasService#getTicketGrantingTicket(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public String getTicketGrantingTicket(final AuthenticationRequest request) {
        return this.getTicketGrantingTicket(request);
    }
}