/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract implementation of HttpRequestEvent that defines the getRequest
 * method so that implementing classes do not need to.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AbstractHttpRequestEvent extends AbstractEvent {

    /** Unique Serializable Id. */
    private static final long serialVersionUID = 4120848858890123065L;

    /**
     * Constructs an AbstractHttpRequestEvent using the HttpServletRequest
     * object as the source.
     * 
     * @param request the HttpServletRequest object that is the source.
     */
    public AbstractHttpRequestEvent(final HttpServletRequest request) {
        super(request);
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
