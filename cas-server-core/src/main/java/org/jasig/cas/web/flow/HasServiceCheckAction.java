/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
        final Service service = WebUtils.getService(getArgumentExtractors(), WebUtils.getHttpServletRequest(context));
        return service != null ? success()
            : error();
    }
}
