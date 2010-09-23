/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;

/**
 * Extends the {@link org.springframework.webflow.context.servlet.DefaultFlowUrlHandler} to support the CAS requirement
 * that tokens be retrieved via the "lt" request parameter.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4
 */
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    @Override
    public String getFlowExecutionKey(final HttpServletRequest request) {
        return request.getParameter("lt");
    }

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final StringBuffer builder = new StringBuffer();
        builder.append(request.getRequestURI());
        builder.append("?");
        appendQueryParameters(builder, request.getParameterMap(), getEncodingScheme(request));
        return builder.toString();
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return request.getRequestURI()
            + (request.getQueryString() != null ? "?"
            + request.getQueryString() : "");
    }
}
