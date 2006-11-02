/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * Implementation of NonInteractiveCredentialsAction that retrieves a
 * ServiceTicket and validates it against a specific CAS server.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ServiceTicketNonInteractiveCredentialsAction extends
    AbstractNonInteractiveCredentialsAction {

    protected Credentials constructCredentialsFromRequest(
        final RequestContext context) {
        final String ticket = WebUtils.getHttpServletRequest(context)
            .getParameter("ticket");

        if (StringUtils.hasText(ticket)) {
            return new ServiceTicketCredentials(ticket);
        }
        return null;
    }
}
