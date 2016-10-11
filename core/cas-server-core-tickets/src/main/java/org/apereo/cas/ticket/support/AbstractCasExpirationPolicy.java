package org.apereo.cas.ticket.support;

import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.web.support.WebUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * This is an {@link AbstractCasExpirationPolicy}
 * that serves as the root parent for all CAS expiration policies
 * and exposes a few internal helper methods to children can access
 * to objects like the request, etc.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractCasExpirationPolicy implements ExpirationPolicy {

    private static final long serialVersionUID = 8042104336580063690L;

    /**
     * Gets the http request based on the
     * {@link org.springframework.web.context.request.RequestContextHolder}.
     * @return the request or null
     */
    protected HttpServletRequest getRequest() {
        return WebUtils.getHttpServletRequest();
    }
}
