/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * Attempts to throttle by both IP Address and username.  Protects against instances where there is a NAT, such as
 * a local campus wireless network.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public final class InMemoryThrottledSubmissionByIpAddressAndUsernameHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    @Override
    protected String constructKey(final HttpServletRequest request, final String usernameParameter) {
        final String username = request.getParameter(usernameParameter);

        if (username == null) {
            return request.getRemoteAddr();
        }

        return request.getRemoteAddr() + ";" + username.toLowerCase();
    }
}
