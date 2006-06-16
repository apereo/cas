/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * The ProxyController is involved with returning a Proxy Ticket (in CAS 2
 * terms) to the calling application. In CAS 3, a Proxy Ticket is just a Service
 * Ticket granted to a service.
 * <p>
 * The ProxyController requires the following property to be set:
 * </p>
 * <ul>
 * <li> centralAuthenticationService - the service layer</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ProxyController extends AbstractController implements
    InitializingBean {

    /** View for if the creation of a "Proxy" Ticket Fails. */
    private static final String CONST_PROXY_FAILURE = "casProxyFailureView";

    /** View for if the creation of a "Proxy" Ticket Succeeds. */
    private static final String CONST_PROXY_SUCCESS = "casProxySuccessView";

    /** CORE to delegate all non-web tier functionality to. */
    private CentralAuthenticationService centralAuthenticationService;

    public ProxyController() {
        setCacheSeconds(0);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null on "
                + this.getClass().getName());
    }

    /**
     * @return ModelAndView containing a view name of either <code>casProxyFailureView</code> or <code>casProxySuccessView</code>
     */
    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String ticket = request
            .getParameter(WebConstants.PROXY_GRANTING_TICKET);
        final String targetService = request
            .getParameter(WebConstants.TARGET_SERVICE);
        final Map model = new HashMap();

        if (!StringUtils.hasText(ticket) || !StringUtils.hasText(targetService)) {
            model.put(WebConstants.CODE, "INVALID_REQUEST");
            model.put(WebConstants.DESC, getMessageSourceAccessor().getMessage(
                "INVALID_REQUEST_PROXY", "INVALID_REQUEST_PROXY"));
            return new ModelAndView(CONST_PROXY_FAILURE, model);
        }

        try {
            final Service service = new SimpleService(WebUtils.stripJsessionFromUrl(targetService));
            return new ModelAndView(CONST_PROXY_SUCCESS, WebConstants.TICKET,
                this.centralAuthenticationService.grantServiceTicket(ticket,
                    service));
        } catch (TicketException e) {
            model.put(WebConstants.CODE, e.getCode());
            model.put(WebConstants.DESC, getMessageSourceAccessor().getMessage(
                e.getCode(), new Object[] {ticket}, e.getCode()));
            return new ModelAndView(CONST_PROXY_FAILURE, model);
        }
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
