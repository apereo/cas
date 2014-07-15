/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides special handling for parameters in requests made to the CAS login
 * webflow.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4
 */
public class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

    @Override
    public String createFlowExecutionUrl(final String flowId, final String flowExecutionKey, final HttpServletRequest request) {
        final StringBuffer builder = new StringBuffer();
        builder.append(request.getRequestURI());
        builder.append("?");
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> parameters = new HashMap<String, String>();
        for(Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if(entry.getValue() != null && entry.getValue().length > 0) {
                parameters.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        appendQueryParameters(builder, parameters, getEncodingScheme(request));
        return builder.toString();
    }

    @Override
    public String createFlowDefinitionUrl(final String flowId, final AttributeMap input, final HttpServletRequest request) {
        return request.getRequestURI()
            + (request.getQueryString() != null ? "?"
            + request.getQueryString() : "");
    }
}
