/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.constants.WebConstants;
import org.jasig.cas.ticket.TicketManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;


/**
 * 
 * Controller to delete ticket granting ticket cookie in order to log out of
 * single sign on.
 * 
 * This controller implements the idea of the ESUP Portail's Logout patch to allow
 * for redirecting to a url on logout.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class LogoutController extends AbstractController {
	protected final Log logger = LogFactory.getLog(getClass());
	private TicketManager ticketManager;
	private String logoutView;
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Cookie cookie = WebUtils.getCookie(request, WebConstants.CONST_COOKIE_TGC_ID);
		String service = request.getParameter(WebConstants.CONST_MODEL_SERVICE);

		if (cookie != null)
		{
			ticketManager.deleteTicket(cookie.getValue());
			destroyTicketGrantingTicketCookie(request, response);
		}
		
		if (service != null) {
			return new ModelAndView(new RedirectView(service));
		} else {
			return new ModelAndView(logoutView);
		}
	}
	
	private void destroyTicketGrantingTicketCookie(HttpServletRequest request, HttpServletResponse response)
	{
		Cookie cookie = new Cookie(WebConstants.CONST_COOKIE_TGC_ID, WebConstants.CONST_COOKIE_DEFAULT_EMPTY_VALUE);
		cookie.setMaxAge(0);
		cookie.setPath(request.getContextPath());
		cookie.setSecure(true);
	    response.addCookie(cookie);
	}

	/**
	 * @param logoutView The logoutView to set.
	 */
	public void setLogoutView(String logoutView)
	{
		this.logoutView = logoutView;
	}
	/**
	 * @param ticketManager The ticketManager to set.
	 */
	public void setTicketManager(TicketManager ticketManager)
	{
		this.ticketManager = ticketManager;
	}
}
