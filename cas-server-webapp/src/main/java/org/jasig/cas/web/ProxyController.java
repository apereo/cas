/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.TicketException;
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
 * <li> casArgumentExtractor - the assistant for extracting parameters</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ProxyController extends AbstractController {

    /** View for if the creation of a "Proxy" Ticket Fails. */
    private static final String CONST_PROXY_FAILURE = "casProxyFailureView";

    /** View for if the creation of a "Proxy" Ticket Succeeds. */
    private static final String CONST_PROXY_SUCCESS = "casProxySuccessView";

    /** Key to use in model for service tickets. */
    private static final String MODEL_SERVICE_TICKET = "ticket";

    /** CORE to delegate all non-web tier functionality to. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    public ProxyController() {
        setCacheSeconds(0);
    }

    /**
     * @return ModelAndView containing a view name of either
     * <code>casProxyFailureView</code> or <code>casProxySuccessView</code>
     */
    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String ticket = request.getParameter("pgt");
        final Service targetService = getTargetService(request);

        if (!StringUtils.hasText(ticket) || targetService == null) {
            return generateErrorView("INVALID_REQUEST",
                "INVALID_REQUEST_PROXY", null);
        }

        try {
            return new ModelAndView(CONST_PROXY_SUCCESS, MODEL_SERVICE_TICKET,
                this.centralAuthenticationService.grantServiceTicket(ticket,
                    targetService));
        } catch (TicketException e) {
            return generateErrorView(e.getCode(), e.getCode(),
                new Object[] {ticket});
        } catch (final UnauthorizedServiceException e) {
            return generateErrorView("UNAUTHORIZED_SERVICE",
                "UNAUTHORIZED_SERVICE_PROXY", new Object[] {targetService});
        }
    }

    private Service getTargetService(final HttpServletRequest request) {
        return SimpleWebApplicationServiceImpl.createServiceFrom(request);
    }

    private ModelAndView generateErrorView(final String code,
        final String description, final Object[] args) {
        final ModelAndView modelAndView = new ModelAndView(CONST_PROXY_FAILURE);
        modelAndView.addObject("code", code);
        modelAndView.addObject("description", getMessageSourceAccessor()
            .getMessage(description, args, description));

        return modelAndView;
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
