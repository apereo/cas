package org.jasig.cas.support.geo;

import java.net.InetAddress;

/**
 * This is {@link GeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface GeoLocationService {

    /**
     * Find a geo location based on an address.
     *
     * @param address the address
     * @return the geo location
     */
    GeoLocation locate(InetAddress address);
}
