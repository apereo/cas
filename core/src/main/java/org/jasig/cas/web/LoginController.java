/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.registry.support.LoginTokenRegistryCleaner;
import org.jasig.cas.util.DefaultUniqueTokenIdGenerator;
import org.jasig.cas.util.UniqueTokenIdGenerator;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.bind.support.DefaultSpringBindCredentialsBinder;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

/**
 * Handle the /login HTTP request.
 * <p>
 * If this URL is presented without Form data, then the showForm method is
 * called. It checks for a Cookie and a prexisting TGT. If none is found then
 * the Form is displayed.
 * </p>
 * <p>
 * The Form is submitted to the processFormSubmission method. It generates a
 * Credentials object and passes it to CAS to generate a TGT.
 * </p>
 * <p>
 * This class requires that the environment inject two properties:
 * </p>
 * <ul>
 * <li>CentralAuthenticationService - a bean that provides the CAS services.</li>
 * <li>LoginTokens - a Map keyed by random strings generated to prevent form
 * resubmission</li>
 * </ul>
 * 
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LoginController extends SimpleFormController implements
    InitializingBean {

    /** Logging. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** CORE to delegate all non-web tier concerns to. */
    private CentralAuthenticationService centralAuthenticationService;

    /** Token Generator for generating login tokens. */
    private UniqueTokenIdGenerator uniqueTokenIdGenerator = null;

    /**
     * Property LoginTokens is initially an empty Map
     * 
     * <p>It will be filled with entries keyed by a randomly generated 
     * string. The string is written to the Form. For a Form submission
     * to be valid, the submitted string must be in the Map. The string
     * is removed from the Map by Form processing, so the Form cannot be
     * resubmmited with the same contents.</p>
     * 
     * <p>It is recommended, but not required, that the Map be periodically
     * cleaned out by expiring old entries. This can be done by an instance
     * of org.jasig.cas.ticket.registry.support.LoginTokenRegistryCleaner.</p>
     * 
     * <p>Access to this Map must be explicitly syncronized.
     * 
     */
    private Map loginTokens;

    /**
     * CredentialsBinder to provide additional bindings besides normal Spring
     * Binding.
     */
    private CredentialsBinder credentialsBinder;

    public LoginController() {
        this.setCacheSeconds(0);
        this.setValidator(new UsernamePasswordCredentialsValidator());
        this.setFormView(ViewNames.CONST_LOGON);
        this.setSuccessView(ViewNames.CONST_LOGON_SUCCESS);
    }
	
	private static final boolean createDefaultWiring = false;
    public void afterPropertiesSet() throws Exception {
        if (this.loginTokens == null) {
			if (!createDefaultWiring) {
				throw new IllegalStateException(
	                "You must set loginTokens on "
	                    + this.getClass());
			} else {
				this.loginTokens= new HashMap();
				LoginTokenRegistryCleaner cleaner = new LoginTokenRegistryCleaner();
				cleaner.setLoginTokens(this.loginTokens);
				cleaner.setTimeOut(43200000);
			}
        }
        if (this.centralAuthenticationService == null) {
            throw new IllegalStateException(
                "You must set centralAuthenticationService on "
                    + this.getClass());
        }

        if (this.uniqueTokenIdGenerator == null) {
            this.uniqueTokenIdGenerator = new DefaultUniqueTokenIdGenerator();
            log
                .info("UniqueIdGenerator not set, using default UniqueIdGenerator of class: "
                    + this.uniqueTokenIdGenerator.getClass());
        }

        if (this.getCommandClass() == null) {
            this.setCommandName("credentials");
            this.setCommandClass(UsernamePasswordCredentials.class);
            log.info("CommandClass not set, using default CommandClass of "
                + this.getCommandClass().getName() + " and name of "
                + this.getCommandName());
        }

        if (this.credentialsBinder == null) {
            this.credentialsBinder = new DefaultSpringBindCredentialsBinder();
            log
                .info("CredentialsBinder not set.  Using default CredentialsBinder of "
                    + this.credentialsBinder.getClass().getName());
        }

        if (!this.credentialsBinder.supports(this.getCommandClass())) {
            throw new ServletException(
                "CredentialsBinder does not support supplied Command Class: "
                    + this.getCommandClass());
        }
    }

    protected Map referenceData(final HttpServletRequest request)
        throws Exception {
        final Map referenceData = new HashMap();

        referenceData.put("loginToken", this.getLoginToken());

        return referenceData;
    }

    /**
     * With no Form data, check for an existing TGT or else display Form.
     */
    protected ModelAndView showForm(final HttpServletRequest request,
        final HttpServletResponse response, final BindException errors)
        throws Exception {
        final String ticketGrantingTicketId = this.getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);
        final boolean warn = this.convertValueToBoolean(this.getCookieValue(
            request, WebConstants.COOKIE_PRIVACY));
        final boolean gateway = StringUtils.hasText(request
            .getParameter(WebConstants.GATEWAY));
        final String service = request.getParameter(WebConstants.SERVICE);
        final boolean renew = this.convertValueToBoolean(request
            .getParameter(WebConstants.RENEW));

        // if we managed to find an existing ticketGrantingTicketId
        if (StringUtils.hasText(ticketGrantingTicketId)
            && StringUtils.hasText(service) && !renew) {
            // we have a service and no request for renew

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service));

                if (warn) {
                    final Map model = new HashMap();

                    model.put(WebConstants.TICKET, serviceTicketId);
                    model.put(WebConstants.SERVICE, service);
                    return new ModelAndView(ViewNames.CONST_LOGON_CONFIRM,
                        model);
                }

                return new ModelAndView(new RedirectView(service),
                    WebConstants.TICKET, serviceTicketId);
            } catch (TicketException e) {
                // nothing to do with this // TODO refactor???
            }
        }

        // if we are being used as a gateway just bounce!
        if (gateway && StringUtils.hasText(service)) {
            return new ModelAndView(new RedirectView(service));
        }

        // otherwise display the logon form
        return super.showForm(request, response, errors);
    }

    /**
     * Process data (userid/password) submitted from the Login Form.
     */
    protected ModelAndView processFormSubmission(
        final HttpServletRequest request, final HttpServletResponse response,
        final Object command, final BindException errors) throws Exception {
        final Credentials credentials = (Credentials) command;
        final boolean renew = this.convertValueToBoolean(request
            .getParameter(WebConstants.RENEW));
        final boolean warn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));
        final String service = request.getParameter(WebConstants.SERVICE);
        final String loginToken = request
            .getParameter(WebConstants.LOGIN_TOKEN);
        String serviceTicketId = null;
        String ticketGrantingTicketId = getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);

        // check for a login ticket
        if (loginToken == null) {
            return super.showForm(request, response, errors);
        }
		
		boolean formReusedOrTimedOut;
		synchronized(this.loginTokens) {
			formReusedOrTimedOut = 
				(null == this.loginTokens.remove(loginToken));
		}
		if (formReusedOrTimedOut) {
			return super.showForm(request, response, errors);
		}
			
        this.credentialsBinder.bind(request, credentials);

        if (renew && StringUtils.hasText(ticketGrantingTicketId)
            && StringUtils.hasText(service)) {

            try {
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
            } catch (TicketException e) {
                // nothing to do here....move on.
            }
        }

        if (serviceTicketId == null) {
            ticketGrantingTicketId = this.centralAuthenticationService
                .createTicketGrantingTicket(credentials);
        }

        this.createCookie(WebConstants.COOKIE_TGC_ID, ticketGrantingTicketId,
            request, response);

        if (warn) {
            this.createCookie(WebConstants.COOKIE_PRIVACY,
                WebConstants.COOKIE_DEFAULT_FILLED_VALUE, request, response);
        } else {
            this.createCookie(WebConstants.COOKIE_PRIVACY,
                WebConstants.COOKIE_DEFAULT_EMPTY_VALUE, request, response);
        }

        if (StringUtils.hasText(service)) {
            // the exception thrown here is handled externally
            serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicketId, new SimpleService(
                    service));

            if (warn) {
                final Map model = new HashMap();

                model.put(WebConstants.TICKET, serviceTicketId);
                model.put(WebConstants.SERVICE, service);
                return new ModelAndView(ViewNames.CONST_LOGON_CONFIRM, model);
            }

            return new ModelAndView(new RedirectView(service),
                WebConstants.TICKET, serviceTicketId);
        }

        return super.processFormSubmission(request, response, command, errors);
    }

    private String getLoginToken() {
        final String loginToken = this.uniqueTokenIdGenerator.getNewTokenId();
		synchronized (this.loginTokens) {
			this.loginTokens.put(loginToken, new Date());
		}

        return loginToken;
    }

    /**
     * Helper method to retrieve the value of a cookie.
     * 
     * @param request The HttpServletRequest containing the cookie.
     * @param cookieId the name of the cookie.
     * @return The value of the cookie or null if it does not exist.
     */
    private String getCookieValue(final HttpServletRequest request,
        final String cookieId) {
        Cookie cookie = WebUtils.getCookie(request, cookieId);

        return (cookie == null) ? null : cookie.getValue();
    }

    /**
     * Method to create a cookie and put it in the response.
     * 
     * @param id The id to name the cookie.
     * @param value The value to give the cookie.
     * @param request The HttpServletRequest
     * @param response TheHttpServletResponse to store the cookie.
     */
    private void createCookie(final String id, final String value,
        final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie cookie = new Cookie(id, value);
        cookie.setSecure(true);
        cookie.setMaxAge(-1);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }

    /**
     * Private method to convert a value to a boolean.
     * 
     * @param value the value to convert.
     * @return either true or false.
     */
    private boolean convertValueToBoolean(final String value) {
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * @param loginTokens an empty Map associated with a RegistryCleaner.
     */
    public void setLoginTokens(final Map loginTokens) {
        this.loginTokens = loginTokens;
    }

    /**
     * @param uniqueTokenIdGenerator The uniqueTokenIdGenerator to set.
     */
    public void setUniqueTokenIdGenerator(
        final UniqueTokenIdGenerator uniqueTokenIdGenerator) {
        this.uniqueTokenIdGenerator = uniqueTokenIdGenerator;
    }

    /**
     * @param credentialsBinder The credentialsBinder to set.
     */
    public void setCredentialsBinder(final CredentialsBinder credentialsBinder) {
        this.credentialsBinder = credentialsBinder;
    }
}
