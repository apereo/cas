/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class providing common functionality for extracting arguments.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    protected final Log log = LogFactory
    .getLog(getClass());
    
    public final String extractTicketArtifact(final RequestContext context) {
        return context.getRequestParameters().get(getArtifactParameterName());
    }

    public final WebApplicationService extractService(RequestContext context) {
        return extractService(WebUtils.getHttpServletRequest(context));
    }

    public final String extractTicketArtifact(final HttpServletRequest request) {
        return request.getParameter(getArtifactParameterName());
    }
    
    protected abstract String getArtifactParameterName();
}
