/*
 * Copyright 2010 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;

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
}
