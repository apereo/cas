/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.webservice.support;

import org.jasig.cas.domain.CasAttributes;
import org.jasig.cas.domain.Principal;
import org.jasig.cas.domain.ServiceTicket;
import org.jasig.cas.domain.TicketGrantingTicket;
import org.jasig.cas.domain.UsernamePasswordAuthenticationRequest;
import org.jasig.cas.service.AuthenticationManager;
import org.jasig.cas.service.TicketManager;
import org.jasig.cas.webservice.CasService;
import org.springframework.validation.DataBinder;


/**
 * Default implementation of the CasService
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class CasServiceImpl implements CasService {
	private TicketManager ticketManager;
	private AuthenticationManager authenticationManager;

	/**
	 * @see org.jasig.cas.webservice.CasService#getServiceTicket(org.jasig.cas.domain.AuthenticationRequest)
	 */
	public String getServiceTicket(UsernamePasswordAuthenticationRequest request, String serviceUrl) {
		final Principal principal;
		final DataBinder dataBinder = new DataBinder(request, "basicAuthenticationRequest");
		final CasAttributes casAttributes = new CasAttributes();
		
		casAttributes.setService(serviceUrl);
		
		dataBinder.setRequiredFields(new String[] {"userName", "password"});
		
		if (dataBinder.getErrors().hasErrors())
			return null;
		
		if ((principal = authenticationManager.authenticateUser(request)) != null)
		{
			TicketGrantingTicket tgt = ticketManager.createTicketGrantingTicket(principal, casAttributes);
			ServiceTicket st = ticketManager.createServiceTicket(principal, casAttributes, tgt);
			
			return st.getId();
		}

		return null;
	}
	/**
	 * @param authenticationManager The authenticationManager to set.
	 */
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	/**
	 * @param ticketManager The ticketManager to set.
	 */
	public void setTicketManager(TicketManager ticketManager) {
		this.ticketManager = ticketManager;
	}
}
