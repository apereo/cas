/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.support;

import org.jasig.cas.services.SingleSignoutCallback;

/**
 * Single sign out callback class to allow single signout to a 
 * Campus Crusade for Christ modified CAS client.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class CCCISingleSignoutCallback implements SingleSignoutCallback {

	public boolean sendSingleSignoutRequest(String serviceTicketId) {
		// TODO Auto-generated method stub
		return false;
	}

}
