/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.WebApplicationService;

/**
 * Strategy interface for retrieving services and tickets from the request.
 * <p>
 * These are the two things the CAS protocol and the SAML protocol have in
 * common.
 * 
 * @author Scott Battatglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface ArgumentExtractor {
    /**
     * Retrieve the service from the request.
     * 
     * @param request the request context.
     * @return the fully formed Service or null if it could not be found.
     */
    WebApplicationService extractService(HttpServletRequest request);
}
