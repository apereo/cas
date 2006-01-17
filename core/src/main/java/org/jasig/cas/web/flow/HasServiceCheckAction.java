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
 * Method to check if a service was provide. If it was, a "hasService" event is
 * returned. Otherwise, the "authenticatedButNoService" event is returned.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class HasServiceCheckAction extends AbstractLoginAction {

    protected Event doExecuteInternal(final RequestContext request,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        if (StringUtils.hasText(service)) {
            return hasService();
        }

        return authenticatedButNoService();
    }
}
