/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import javax.servlet.http.HttpServletRequest;

/**
 * Event representing an HttpServletRequest.  This event can be
 * used to represent any event from a web request.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public interface HttpRequestEvent extends Event {
    
    /**
     * Method to retrieve the HttpServletRequest.
     * @return the HTTP ServletRequest.
     */
    HttpServletRequest getRequest();
}
