/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.webservice.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;


/**
 * Remote CasService interface required for JAX-RPC
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface RemoteCasService extends Remote
{
	/**
	 * 
	 * Method that if provided the proper credentials will return a service ticket.
	 * 
	 * @param request The authentication request to retrieve a CAS service ticket
	 * @return A CAS service ticket or null.
	 * @throws RemoteException
	 */
	String getServiceTicket(UsernamePasswordAuthenticationRequest request, String serviceUrl) throws RemoteException;
}
