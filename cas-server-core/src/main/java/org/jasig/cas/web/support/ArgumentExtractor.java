/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.webflow.execution.RequestContext;

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
     * Retrieve the service from the request context.
     * 
     * @param context the request context.
     * @return the fully formed Service or null if it could not be found.
     */
    Service extractService(RequestContext context);

    /**
     * Retrieve the ticket from the request context.
     * 
     * @param context the request context.
     * @return the ticket or null if it could not be found.
     */
    String extractTicketArtifact(RequestContext context);

    /**
     * Constructs the URL to redirect to after the generation of the service
     * ticket.
     * 
     * @param context the request context
     * @return The fully qualified URL if one can be constructed. Otherwise,
     * return null.
     */
    String constructUrlForRedirct(RequestContext context);

    /**
     * Retrieve the ticket from the request.
     * 
     * @param request the request.
     * @return the ticket or null if it could not be found.
     */
    String extractTicketArtifact(HttpServletRequest request);

    /**
     * Retrieve the service from the request.
     * 
     * @param request the request context.
     * @return the fully formed Service or null if it could not be found.
     */
    Service extractService(HttpServletRequest request);

}
