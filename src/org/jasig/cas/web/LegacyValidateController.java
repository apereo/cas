/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketManager;
import org.jasig.cas.ticket.validation.ValidationRequest;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * 
 * Controller to handle legacy validation (1.0??)
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class LegacyValidateController extends AbstractController {
	protected final Log log = LogFactory.getLog(getClass());
	private TicketManager ticketManager;
	
	public LegacyValidateController() {
		setCacheSeconds(0);
	}
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ValidationRequest validationRequest = new ValidationRequest();
		PrintWriter out = response.getWriter();
		ServiceTicket serviceTicket;

		BindUtils.bind(request, validationRequest, "validationRequest");
		
		log.info("Attempting to retrieve valid ServiceTicket for [" + validationRequest.getTicket());
		serviceTicket = this.ticketManager.validateServiceTicket(validationRequest);
		
		if (serviceTicket == null) {
			log.info("Unable to retrieve ServiceTicket for ticket id [" + validationRequest.getTicket() + "] and service [" + validationRequest.getService() + "]");
			out.print("no\n\n");
		}
		else {
			log.info("Successfully retrieved ServiceTicket for ticket id [" + validationRequest.getTicket() + "] and service [" + validationRequest.getService() + "]");
			out.print("yes\n" + serviceTicket.getPrincipal().getId()+"\n");
		}
		
		out.flush();
		
		return null;
	}

	/**
	 * @param ticketManager The ticketManager to set.
	 */
	public void setTicketManager(TicketManager ticketManager)
	{
		this.ticketManager = ticketManager;
	}
}
