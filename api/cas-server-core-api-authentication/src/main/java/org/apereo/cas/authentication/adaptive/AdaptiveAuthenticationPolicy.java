package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;

import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AdaptiveAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AdaptiveAuthenticationPolicy {

    /**
     * Apply the strategy to figure out whether this authentication attempt can proceed.
     *
     * @param requestContext the request context
     * @param userAgent      the user agent
     * @param location       the location
     * @return true /false
     */
    boolean apply(RequestContext requestContext, String userAgent, GeoLocationRequest location);
}
