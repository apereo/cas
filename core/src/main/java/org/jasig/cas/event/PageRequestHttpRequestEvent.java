/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class PageRequestHttpRequestEvent extends AbstractHttpRequestEvent {

    public PageRequestHttpRequestEvent(HttpServletRequest request) {
        super(request);
    }
    
    /**
     * Convenience method to return just the page requested stripped of any extra context or querystring information.
     * @return the truncated page requested.
     */
    public String getPage() {
        final String requestUri = getRequest().getRequestURI();
        final String requestContext = getRequest().getContextPath();
        return requestUri.substring(requestUri.indexOf(requestContext)+requestContext.length()+1);
    }

}
