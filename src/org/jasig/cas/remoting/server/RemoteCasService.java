/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jasig.cas.authentication.AuthenticationRequest;

/**
 * Remote CasService interface required for JAX-RPC
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface RemoteCasService extends Remote {

    String getTicketGrantingTicket(final AuthenticationRequest request) throws RemoteException;
    
    String getServiceTicket(final String ticketGrantingTicketId, final String service) throws RemoteException;
}
