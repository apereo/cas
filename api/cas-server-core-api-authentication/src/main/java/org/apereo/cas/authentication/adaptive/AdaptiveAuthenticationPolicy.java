package org.apereo.cas.authentication.adaptive;

import module java.base;
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
     * Default bean name.
     */
    String BEAN_NAME = "adaptiveAuthenticationPolicy";

    /**
     * Apply the strategy to figure out whether this authentication attempt can proceed.
     *
     * @param requestContext the request context
     * @param userAgent      the user agent
     * @param location       the location
     * @return true/false
     * @throws Throwable the throwable
     */
    boolean isAuthenticationRequestAllowed(RequestContext requestContext, String userAgent, GeoLocationRequest location) throws Throwable;
}
