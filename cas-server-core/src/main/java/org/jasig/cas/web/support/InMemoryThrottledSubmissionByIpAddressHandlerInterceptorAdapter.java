/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

/**
 * Throttles access attempts for failed logins by IP Address.  This stores the attempts in memory.  This is not good for a
 * clustered environment unless the intended behavior is that this blocking is per-machine.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public final class InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter extends AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter {

    @Override
    protected String constructKey(final HttpServletRequest request, final String usernameParameter) {
        return request.getRemoteAddr();
    }
}
