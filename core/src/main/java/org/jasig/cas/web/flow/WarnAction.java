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

    /** Event to publish in the scenario when just a redirect is required. */
    private static final String EVENT_REDIRECT = "redirect";

    protected Event doExecute(final RequestContext context) throws Exception {
        return getCasArgumentExtractor().isWarnCookiePresent(context)
            ? result(EVENT_WARN) : result(EVENT_REDIRECT);
    }
}
