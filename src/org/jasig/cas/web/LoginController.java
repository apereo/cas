/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.domain.AuthenticationRequest;
import org.jasig.cas.domain.CasAttributes;
import org.jasig.cas.domain.Principal;
import org.jasig.cas.domain.TicketGrantingTicket;
import org.jasig.cas.domain.ValidationRequest;
import org.jasig.cas.domain.logic.BasicAuthenticationRequestValidator;
import org.jasig.cas.helper.UniqueTokenIdGenerator;
import org.jasig.cas.helper.support.CommonsIdUniqueTokenIdGenerator;
import org.jasig.cas.service.AuthenticationManager;
import org.jasig.cas.service.TicketManager;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;


/**
 * 
 * Controller to handle login to CAS.  Current flow is as follows:
 * 1.  If ticket granting ticket exists and not a renew, forward.
 * 2.  If ticket granting ticket exists but is a renew then check
 * 	a.  if same username, access granted
 *  b.  otherwise expire ticket and process login.
 * 3.  On authentication, if for a service, redirect to the service.
 * 4.  Otherwise, generic CAS sign on.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class LoginController extends AbstractFormController {
	protected final Log logger = LogFactory.getLog(getClass());
	private String formView;
	private String successView;
	private String confirmView;
	private TicketManager ticketManager;
	private AuthenticationManager authenticationManager;
	private Map loginTokens;
	private UniqueTokenIdGenerator idGenerator = new CommonsIdUniqueTokenIdGenerator();

	public LoginController() {
		setCacheSeconds(0);
		this.setValidator(new BasicAuthenticationRequestValidator());
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		AuthenticationRequest authRequest = (AuthenticationRequest) command;
		CasAttributes casAttributes = new CasAttributes();
		ValidationRequest validationRequest = new ValidationRequest();
		BindUtils.bind(request, authRequest, "authenticationRequest");
		BindUtils.bind(request, validationRequest, "validationRequest");
		BindUtils.bind(request, casAttributes, "casAttributes");
		Principal principal;
		
		/*
		TicketGrantingTicket ticket = getTicketGrantingTicket(request, validationRequest);

		ModelAndView mv = getViewToForwardTo(request, ticket, casAttributes, validationRequest);
		if (mv != null)
			return mv;*/

		String loginToken = request.getParameter(WebConstants.CONST_MODEL_TICKET);

		if (!loginTokens.containsKey(loginToken)) {
			logger.info("Duplicate login detected for Authentication Request [" + authRequest + "]");
			errors.reject("error.invalid.loginticket", null);
			return showForm(request, response, errors);
		} else
			loginTokens.remove(loginToken);

		principal = authenticationManager.authenticateUser(authRequest);

		if (principal != null) {
			logger.info("Successfully authenticated user [" + authRequest + "] to principal with Id [" + principal.getId() + "]");

			TicketGrantingTicket ticket = getTicketGrantingTicket(request, principal, validationRequest);
			
			if (ticket == null) {
				logger.info("Creating new ticket granting ticket for principal [" + principal.getId() + "]");
				ticket = ticketManager.createTicketGrantingTicket(principal, casAttributes);
				createCookie(WebConstants.CONST_COOKIE_TGC_ID, ticket.getId(), request, response);
			}
			if (casAttributes.isWarn())
				createCookie(WebConstants.CONST_COOKIE_PRIVACY, WebConstants.CONST_COOKIE_DEFAULT_FILLED_VALUE, request, response);
			else
				createCookie(WebConstants.CONST_COOKIE_PRIVACY, WebConstants.CONST_COOKIE_DEFAULT_EMPTY_VALUE, request, response);

			casAttributes.setFirst(true);
			return grantForService(request, ticket, casAttributes);
		} else {
			errors.reject("error.username.and.password", null);
			return showForm(request, response, errors);
		}
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		Map map = new HashMap();
		String newToken = idGenerator.getNewTokenId();
		loginTokens.put(newToken, new Date());
		map.put(WebConstants.CONST_MODEL_LOGIN_TICKET, newToken);
		return map;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#showForm(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.validation.BindException)
	 */
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {

		ValidationRequest validationRequest = new ValidationRequest();
		CasAttributes casAttributes = new CasAttributes();
		Map model = new HashMap();

		BindUtils.bind(request, validationRequest, "validationRequest");
		BindUtils.bind(request, casAttributes, "casAttributes");
		TicketGrantingTicket ticket = getTicketGrantingTicket(request, null, validationRequest); // assume no principal since we haven't posted to the form  yet.
		ModelAndView mv = getViewToForwardTo(request, ticket, casAttributes, validationRequest);

		if (mv != null)
			return mv;

		model.put(WebConstants.CONST_MODEL_CAS_ATTRIBUTES, casAttributes);
		return super.showForm(request, errors, formView, model);
	}

	private ModelAndView getViewToForwardTo(HttpServletRequest request, TicketGrantingTicket ticket, CasAttributes casAttributes, ValidationRequest validationRequest) {
		if (ticket != null) {
			if (!validationRequest.isRenew()) {
				casAttributes.setFirst(false);
				return grantForService(request, ticket, casAttributes);
			}
			if (StringUtils.hasText(casAttributes.getGateway()) && StringUtils.hasText(validationRequest.getService()))
				return new ModelAndView(new RedirectView(validationRequest.getService()));
		}
		return null;
	}

	private void createCookie(String id, String value, HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = new Cookie(id, value);
		cookie.setSecure(true);
		cookie.setMaxAge(-1);
		cookie.setPath(request.getContextPath());
		response.addCookie(cookie);
	}

	private ModelAndView grantForService(final HttpServletRequest request, final TicketGrantingTicket ticket, CasAttributes casAttributes) {
		Map model = new HashMap();
		String service = casAttributes.getService();
		boolean first = casAttributes.isFirst();
		if (StringUtils.hasText(service)) {
			String token = ticketManager.createServiceTicket(ticket.getPrincipal(), casAttributes, ticket).getId();
			model.put(WebConstants.CONST_MODEL_TICKET, token);
			model.put(WebConstants.CONST_MODEL_SERVICE, service);
			model.put(WebConstants.CONST_MODEL_FIRST, new Boolean(first).toString());
			if (!first) {
				if (privacyRequested(request))
					return new ModelAndView(confirmView, model);
				else {
					model.remove(WebConstants.CONST_MODEL_SERVICE);
					return new ModelAndView(new RedirectView(service), model);
				}
			} else
				return new ModelAndView(new RedirectView(service), model);
		}
		return new ModelAndView(successView);
	}

	private boolean privacyRequested(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, WebConstants.CONST_COOKIE_PRIVACY);
		if (cookie == null)
			return false;
		return Boolean.getBoolean(cookie.getValue());
	}

	private TicketGrantingTicket getTicketGrantingTicket(final HttpServletRequest request, final Principal principal, final ValidationRequest validationRequest) {
		Cookie tgt = WebUtils.getCookie(request, WebConstants.CONST_COOKIE_TGC_ID);
		validationRequest.setPrincipal(principal);
		TicketGrantingTicket ticket = null;
		if (tgt != null) {
			validationRequest.setTicket(tgt.getValue());
			ticket = ticketManager.validateTicketGrantingTicket(validationRequest);
		}
		return ticket;
	}

	/**
	 * @param authenticationManager The authenticationManager to set.
	 */
	public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	/**
	 * @param confirmView The confirmView to set.
	 */
	public void setConfirmView(final String confirmView) {
		this.confirmView = confirmView;
	}

	/**
	 * @param formView The formView to set.
	 */
	public void setFormView(final String formView) {
		this.formView = formView;
	}

	/**
	 * @param successView The successView to set.
	 */
	public void setSuccessView(final String successView) {
		this.successView = successView;
	}

	/**
	 * @param ticketManager The ticketManager to set.
	 */
	public void setTicketManager(final TicketManager ticketManager) {
		this.ticketManager = ticketManager;
	}

	/**
	 * @param idGenerator The idGenerator to set.
	 */
	public void setIdGenerator(final UniqueTokenIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	/**
	 * @param loginTokens The loginTokens to set.
	 */
	public void setLoginTokens(final Map loginTokens) {
		this.loginTokens = loginTokens;
	}
}