/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Assertion;
import org.jasig.cas.authentication.Cas10ProtocolAuthenticationSpecification;
import org.jasig.cas.authentication.SimpleService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Controller to handle legacy validation (1.0)
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class LegacyValidateController extends AbstractController implements InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private CentralAuthenticationService centralAuthenticationService;

    public LegacyValidateController() {
        setCacheSeconds(0);
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.centralAuthenticationService == null) {
            throw new IllegalStateException("centralAuthenticationService cannot be null on " + this.getClass().getName());
        }
    }

    /**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final String serviceTicketId = request.getParameter(WebConstants.TICKET);
        final String service = request.getParameter(WebConstants.SERVICE);
        final PrintWriter out = response.getWriter();
        final Assertion assertion;
        final Cas10ProtocolAuthenticationSpecification authenticationSpecification = new Cas10ProtocolAuthenticationSpecification(StringUtils
            .hasText(request.getParameter(WebConstants.RENEW)));

        log.info("Attempting to retrieve valid ServiceTicket for [" + serviceTicketId + "]");

        try {
            assertion = centralAuthenticationService.validateServiceTicket(serviceTicketId, new SimpleService(service), authenticationSpecification);

            log.info("Successfully retrieved ServiceTicket for ticket id [" + serviceTicketId + "] and service [" + service + "]");
            out.print("yes\n" + ((Principal) assertion.getChainedPrincipals().get(0)).getId() + "\n");
        }
        catch (TicketException te) {
            log.info("Unable to retrieve ServiceTicket for ticket id [" + serviceTicketId + "] and service [" + service + "]");
            out.print("no\n\n");
        }

        out.flush();

        return null;
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to set.
     */
    public void setCentralAuthenticationService(CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
