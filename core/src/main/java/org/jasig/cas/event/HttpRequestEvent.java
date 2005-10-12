/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of an HttpRequestEvent that adds convenience methods to log
 * page accesses. PageRequestHttpRequestEvents provide access to the page,
 * IpAddress, User Agent, Http Method type, and the referrer.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class HttpRequestEvent extends AbstractEvent {

    /** The Unique Serializable Id. */
    private static final long serialVersionUID = 3257290244557910064L;

    /** The constant representing the user agent header. */
    private static final String HEADER_USER_AGENT = "User-Agent";

    /** the constant representing the referer header. */
    private static final String HEADER_REFERRER = "Referer";

    /**
     * Constructs a PageRequestHttpRequestEvent with the HttpServletRequest as
     * the source.
     * 
     * @param request the HttpServletRequest to use as the source.
     */
    public HttpRequestEvent(final HttpServletRequest request) {
        super(request);
    }

    /**
     * Convenience method to return just the page requested stripped of any
     * extra context or querystring information.
     * 
     * @return the truncated page requested.
     */
    public final String getPage() {
        final String requestUri = getRequest().getRequestURI();
        final String requestContext = getRequest().getContextPath();
        return requestUri.substring(requestUri.indexOf(requestContext)
            + requestContext.length() + 1);
    }

    /**
     * Convenience method to return the IPAddress.
     * 
     * @return the IP Address of the remote user.
     */
    public final String getIpAddress() {
        return getRequest().getRemoteAddr();
    }

    /**
     * Convenience method to return the type of request.
     * 
     * @return GET or POST in most cases.
     */
    public final String getMethod() {
        return getRequest().getMethod();
    }

    /**
     * Convenience method to return the user agent.
     * 
     * @return the string from the User Agent header.
     */
    public final String getUserAgent() {
        return getRequest().getHeader(HEADER_USER_AGENT);
    }

    /**
     * Convenience method to return the referrer. Note, that this method name
     * uses the proper header.
     * 
     * @return the referrer if there is one, null otherwise.
     */
    public final String getReferrer() {
        return getRequest().getHeader(HEADER_REFERRER);
    }

    /**
     * Method to retrieve HttpServletRequest.
     * 
     * @return the HttpServletRequest for this event.
     */
    public final HttpServletRequest getRequest() {
        return (HttpServletRequest) getSource();
    }
}
