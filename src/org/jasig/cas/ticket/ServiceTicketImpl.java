/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;


/**
 * Domain object representing a Service Ticket.  A service ticket grants
 * specific access to a particular service.  It will only work for a particular
 * service.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ServiceTicketImpl extends AbstractTicket implements ServiceTicket {
	private static final long serialVersionUID = 1296808733190507408L;
	final private TicketGrantingTicket grantor;
	final private String service;
	final private boolean fromNewLogin;

	public ServiceTicketImpl(final String id, final TicketGrantingTicket ticket, final String service, final boolean fromNewLogin, final ExpirationPolicy policy) {
		super(id, ticket.getPrincipal(), policy);
		if (ticket == null || service == null)
			throw new IllegalArgumentException("ticket and service are required parameters");
		this.grantor = ticket;
		this.service = service;
		this.fromNewLogin = fromNewLogin;
	}

	/**
	 * @return Returns the fromNewLogin.
	 */
	public boolean isFromNewLogin() {
		return fromNewLogin;
	}

	/**
	 * @return Returns the grantor.
	 */
	public TicketGrantingTicket getGrantor() {
		return grantor;
	}

	/**
	 * @return Returns the service.
	 */
	public String getService() {
		return service;
	}

	/**
	 * @see org.jasig.cas.ticket.Ticket#isExpired()
	 */
	public boolean isExpired() {
		return super.isExpired() || grantor.isExpired();
	}
}
