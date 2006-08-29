/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
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

    private static final String EVENT_AUTHENTICATION_REQUIRED = "authenticationRequired";

    protected Event doExecute(final RequestContext context) {
        return isRenewPresent(context)
            ? result(EVENT_AUTHENTICATION_REQUIRED)
            : result(EVENT_GENERATE_SERVICE_TICKET);
    }
}
