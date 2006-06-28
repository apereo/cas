/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Action for determining whether the warning page needs to be displayed or not.
 * If it does not need to be displayed we want to forward to the proper service.
 * If there is a privacy request for a warning, the "warn" event is returned,
 * otherwise the "redirect" event is returned.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class WarnAction extends AbstractLoginAction {

    /** Event to publish in the scenario where a warning is required. */
    private static final String EVENT_WARN = "warn";

    protected Event doExecuteInternal(final RequestContext context,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {
        return warn ? warn() : redirect();
    }

    /**
     * Method to generate an event for a warning.
     * 
     * @return an event symbolized by the word "warn".
     */
    private Event warn() {
        return result(EVENT_WARN);
    }
}
