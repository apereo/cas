/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Assertion;
import org.jasig.cas.authentication.AuthenticationSpecification;
import org.jasig.cas.authentication.Cas10ProtocolAuthenticationSpecification;
import org.jasig.cas.authentication.Cas20ProtocolAuthenticationSpecification;
import org.jasig.cas.authentication.SimpleService;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Controller to validate ServiceTickets and ProxyGrantingTickets.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class ServiceValidateController extends AbstractController implements InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private CentralAuthenticationService centralAuthenticationService;
    
    private boolean cas10protocol = false;
    
    private Class authenticationSpecificationClass;
    
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.centralAuthenticationService == null) {
            throw new IllegalStateException("centralAuthenticationService cannot be null on " + this.getClass().getName());
        }
        
        if (cas10protocol) {
        	this.authenticationSpecificationClass = Cas10ProtocolAuthenticationSpecification.class;
        }
        
        if (this.authenticationSpecificationClass == null) {
        	this.authenticationSpecificationClass = Cas20ProtocolAuthenticationSpecification.class;
        	log.info("No authentication specification class set.  Defaulting to " + this.authenticationSpecificationClass.getClass().getName());
        }
    }
    /**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String serviceTicketId = request.getParameter(WebConstants.TICKET);
        final String service = request.getParameter(WebConstants.SERVICE);
        final Map model = new HashMap();
        final AuthenticationSpecification authenticationSpecification = this.getCommandClass();
        final Assertion assertion;
        
        BindUtils.bind(request, authenticationSpecification, "authenticationSpecification");

        try {
            assertion = centralAuthenticationService.validateServiceTicket(serviceTicketId, new SimpleService(service));

           if (!authenticationSpecification.isSatisfiedBy(assertion)) {
                log.debug("ServiceTicket [" + serviceTicketId + "] does not satisfy authentication specification.");
                throw new TicketException(TicketException.INVALID_TICKET, "ticket not backed by initial CAS login, as requested");
           }
            
            model.put(WebConstants.PRINCIPAL, assertion.getChainedPrincipals().get(0));
            
            if (assertion.getChainedPrincipals().size() > 1) {
                final List proxies = new ArrayList();
                
                final Iterator iter = assertion.getChainedPrincipals().iterator();
                iter.next();
                
                while (iter.hasNext()) {
                    proxies.add(iter.next());
                }
                
                model.put(WebConstants.PROXIES, Collections.unmodifiableList(proxies));
            }
            if (this.cas10protocol) {
        		final PrintWriter out = response.getWriter();
                log.info("Successfully retrieved ServiceTicket for ticket id [" + serviceTicketId + "] and service [" + service + "]");
                out.print("yes\n" + ((Principal) assertion.getChainedPrincipals().get(0)).getId() + "\n");
                return null;
            }

            return new ModelAndView(ViewNames.CONST_SERVICE_SUCCESS, model);
        }
        catch (TicketException te) {
        	if (this.cas10protocol) {
        		final PrintWriter out = response.getWriter();
                log.info("Unable to retrieve ServiceTicket for ticket id [" + serviceTicketId + "] and service [" + service + "]");
                out.print("no\n\n");
                out.flush();
                return null;
        	}

            model.put(WebConstants.CODE, te.getCode());
            model.put(WebConstants.DESC, te.getDescription());

            return new ModelAndView(ViewNames.CONST_SERVICE_FAILURE, model);
        }

        /*

         if (validationRequest.getPgtUrl() != null) {
         log.info("Creating ProxyGranting Ticket for ServiceTicket [" + validationRequest.getTicket() + ".");
         ProxyGrantingTicket proxyGrantingTicket = this.ticketManager.createProxyGrantingTicket(casAttributes,
         serviceTicket);
         model.put(WebConstants.PGTIOU, proxyGrantingTicket.getProxyIou());
         }
         */
        // TODO implement
    }

    private AuthenticationSpecification getCommandClass() {
    	try {
    	return (AuthenticationSpecification) this.authenticationSpecificationClass.newInstance();
    	} catch (Exception e) {
    		throw new IllegalStateException("Unable to instanciate instance of " + this.authenticationSpecificationClass.getClass().getName());
    	}
    }
    
    
    /**
     * @param centralAuthenticationService The centralAuthenticationService to set.
     */
    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
