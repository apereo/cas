/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler;

import org.jasig.cas.authentication.AuthenticationRequest;

/**
 * Interface for the handlers that will authenticate a user against a resource.
 * CAS Developers would implement their own AuthenticationHandler and plug it into the 
 * AuthenticationManager to authenticate users against their own resources such as
 * a database table.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface AuthenticationHandler {
	
	/**
	 * 
	 * Method to authenticate a request.
	 * 
	 * @param request the request to authenticate
	 * @return true if the request is valid, false otherwise.
	 */
	boolean authenticate(AuthenticationRequest request);
	
	/**
	 * 
	 * @param request The request we want to check if the handler supports.
	 * @return true if the handler supports authenticating this type of request.  False otherwise.
	 */
	boolean supports(AuthenticationRequest request);
}
