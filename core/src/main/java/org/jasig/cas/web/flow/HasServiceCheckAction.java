/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Method to check if a service was provide. If it was, a "success" event is
 * returned. Otherwise, the "error" event is returned.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class HasServiceCheckAction extends AbstractLoginAction {

    protected Event doExecute(final RequestContext context) {
        return getCasArgumentExtractor().isServicePresent(context) ? success()
            : error();
    }
}
