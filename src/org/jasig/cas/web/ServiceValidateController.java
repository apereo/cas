/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketManager;
import org.jasig.cas.ticket.validation.ValidationRequest;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * 
 * Controller to validate ServiceTickets and ProxyGrantingTickets.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ServiceValidateController extends AbstractController
{
	protected final Log log = LogFactory.getLog(getClass());
	private TicketManager ticketManager;

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		ValidationRequest validationRequest = new ValidationRequest();
		CasAttributes casAttributes = new CasAttributes();
		Map model = new HashMap();
		ServiceTicket serviceTicket;
		BindUtils.bind(request, validationRequest, "validationRequest");

		this.log.info("Attempting to retrieve a valid ServiceTicket for [" + validationRequest.getTicket() + "]");
		serviceTicket = this.ticketManager.validateServiceTicket(validationRequest);
		
		if (serviceTicket == null)
		{
			this.log.info("ServiceTicket [" + validationRequest.getTicket() + "was invalid.");
			model.put(WebConstants.CODE, "INVALID_TICKET");
			model.put(WebConstants.DESC, "ticket '" + validationRequest.getTicket() + "' not recognized.");
			return new ModelAndView(ViewNames.CONST_SERVICE_FAILURE, model);
		}
		else
		{
			this.log.info("ServiceTicket [" + validationRequest.getTicket() + "was valid.");
			if (validationRequest.getPgtUrl() != null)
			{
				this.log.info("Creating ProxyGranting Ticket for ServiceTicket [" + validationRequest.getTicket() + ".");
				ProxyGrantingTicket proxyGrantingTicket = this.ticketManager.createProxyGrantingTicket(serviceTicket.getPrincipal(), casAttributes, serviceTicket);
				model.put(WebConstants.PGTIOU, proxyGrantingTicket.getProxyIou());
			}
		}
		
		if (serviceTicket instanceof ProxyTicket) {
			ProxyTicket p = (ProxyTicket)  serviceTicket;
			model.put(WebConstants.PROXIES, p.getProxies());
		}
		
		model.put(WebConstants.PRINCIPAL, serviceTicket.getPrincipal());
		
		return new ModelAndView(ViewNames.CONST_SERVICE_SUCCESS, model);
	}

	/**
	 * @param this.ticketManager The this.ticketManager to set.
	 */
	public void setTicketManager(TicketManager ticketManager)
	{
		this.ticketManager = ticketManager;
	}
}
