/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.Collections;
import java.util.List;




/**
 * Domain object representing a proxy ticket.
 *  
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ProxyTicketImpl extends ServiceTicketImpl implements ProxyTicket {
	private static final long serialVersionUID = -8941195377431478700L;

	/**
	 * @param id
	 * @param ticket
	 * @param service
	 * @param policy
	 */
	public ProxyTicketImpl(final String id, final ProxyGrantingTicket ticket, final String service, final ExpirationPolicy policy) {
		super(id, ticket, service, false, policy);
	}
	
	public List getProxies() {
		return Collections.unmodifiableList(((ProxyGrantingTicket) this.getGrantor()).getProxies());
	}
}