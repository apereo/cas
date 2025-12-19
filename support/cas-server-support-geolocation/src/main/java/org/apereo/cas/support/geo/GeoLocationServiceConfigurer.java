package org.apereo.cas.support.geo;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;

/**
 * This is {@link GeoLocationServiceConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface GeoLocationServiceConfigurer {
    /**
     * Configure.
     *
     * @return the account registration provisioner
     */
    GeoLocationService configure();
}
