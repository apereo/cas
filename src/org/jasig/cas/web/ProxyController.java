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
import org.jasig.cas.ticket.TicketManager;
import org.jasig.cas.ticket.validation.ValidationRequest;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * 
 * Controller to return a valid proxy ticket upon request.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *  
 */
public class ProxyController extends AbstractController {
    protected final Log log = LogFactory.getLog(getClass());

    private TicketManager ticketManager;

    public ProxyController() {
        setCacheSeconds(0);
    }

    /**
     * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ValidationRequest validationRequest = new ValidationRequest();
        CasAttributes casAttributes = new CasAttributes();
        ProxyGrantingTicket ticket;
        ProxyTicket proxyTicket;
        Map model = new HashMap();

        BindUtils.bind(request, validationRequest, "validationRequest");
        BindUtils.bind(request, casAttributes, "casAttributes");
        validationRequest.setTicket(validationRequest.getPgt());

        this.log.info("Attempting to retrieve valid ProxyGrantingTicket for ticket id [" + validationRequest.getTicket() + "]");
        ticket = this.ticketManager.validateProxyGrantingTicket(validationRequest);

        if (ticket != null) {
            this.log.info("Obtained valid ProxyGrantingTicket for ticket id [" + validationRequest.getTicket() + "]");
            proxyTicket = this.ticketManager.createProxyTicket(ticket.getPrincipal(), casAttributes, ticket);
            model.put(WebConstants.TICKET, proxyTicket.getId());
            return new ModelAndView(ViewNames.CONST_PROXY_SUCCESS, model);
        } else {
            this.log.info("Unable to obtain valid ProxyGrantingTicket for ticket id [" + validationRequest.getTicket() + "]");
            model.put(WebConstants.CODE, "BAD_PGT");
            model.put(WebConstants.DESC, "unrecognized pgt: " + validationRequest.getTicket());
            return new ModelAndView(ViewNames.CONST_PROXY_FAILURE, model);
        }
    }

    /**
     * @param ticketManager The ticketManager to set.
     */
    public void setTicketManager(TicketManager ticketManager) {
        this.ticketManager = ticketManager;
    }
}