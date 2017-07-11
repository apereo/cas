package org.apereo.cas.web;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link TokenRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface TokenRequestExtractor {

    /**
     * Extract string.
     *
     * @param request the request
     * @return the string
     */
    String extract(HttpServletRequest request);
}
