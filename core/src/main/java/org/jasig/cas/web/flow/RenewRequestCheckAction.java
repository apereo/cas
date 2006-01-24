/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action to check if the renew flag has been set. If it has, notify that
 * authentication is required. Otherwise, notify that a ServiceTicket can be
 * generated.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class RenewRequestCheckAction extends AbstractLoginAction {

    /** Event string denoting that we should generate a service ticket. */
    private static final String EVENT_GENERATE_SERVICE_TICKET = "generateServiceTicket";

    protected Event doExecuteInternal(final RequestContext request,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        if (renew) {
            return authenticationRequired();
        }

        return generateServiceTicket();
    }

    /**
     * Method to create a "generateServiceTicket" event.
     * 
     * @return the event to notify of a generateServiceTicket request.
     */
    private Event generateServiceTicket() {
        return result(EVENT_GENERATE_SERVICE_TICKET);
    }
}
