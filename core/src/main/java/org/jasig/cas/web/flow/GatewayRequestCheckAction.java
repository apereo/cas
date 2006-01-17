/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action to check if there is a gateway set and there is a service to redirect
 * to. If so, a <code>gateway</code> event is published to denote that we
 * should gateway.
 * <p>
 * Otherwise, an <code>authenticationRequired</code> event is published.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class GatewayRequestCheckAction extends AbstractLoginAction {

    protected Event doExecuteInternal(final RequestContext request,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        if (gateway && StringUtils.hasText(service)) {
            return gateway();
        }

        return authenticationRequired();
    }
}
