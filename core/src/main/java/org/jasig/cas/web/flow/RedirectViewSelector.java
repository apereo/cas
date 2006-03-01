/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.ViewSelection;
import org.springframework.webflow.ViewSelector;

/**
 * Custom View Selector that allows the Web Flow to have an end state that
 * allows for redirects based on a URL provided rather than just configured
 * views.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RedirectViewSelector implements
    ViewSelector {

    /**
     * @return ViewDescriptor constructed from a ServiceUrl stored in the
     * RequestScope as WebConstants.SERVICE and the model consisting of the
     * ticket id stored in the request scope.
     */
    public ViewSelection makeSelection(final RequestContext context) {
        final String service = WebUtils.getRequestParameterAsString(
            ContextUtils.getHttpServletRequest(context), WebConstants.SERVICE);
        final String ticket = (String) ContextUtils.getAttribute(context,
            WebConstants.TICKET);

        if (ticket != null) {
            final Map model = new HashMap();
            model.put(WebConstants.TICKET, ticket);
            
            final ViewSelection descriptor = new ViewSelection(service,
                model, true);
            return descriptor;
        }
        
        return new ViewSelection(service, new HashMap(), true);
    }
}
