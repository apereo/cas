package org.apereo.cas.authentication.adaptive;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;

/**
 * This is {@link AdaptiveAuthenticationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface AdaptiveAuthenticationStrategy {
    
    /**
     * Apply the strategy to figure out whether this authentication attempt can proceed.
     *
     * @param userAgent the user agent
     * @param location  the location
     * @return true/false
     */
    boolean apply(String userAgent, GeoLocationRequest location);
}
