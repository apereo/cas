package org.apereo.cas.authentication.adaptive.geo;

import java.net.InetAddress;

/**
 * This is {@link GeoLocationService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface GeoLocationService {

    /**
     * Find a geo location based on an address.
     *
     * @param address the address
     * @return the geo location
     */
    GeoLocationResponse locate(InetAddress address);

    /**
     * Find a geo location based on an address.
     *
     * @param ipAddress the address
     * @return the geo location
     */
    GeoLocationResponse locate(String ipAddress);

    /**
     * Find a geo location based on an address.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return the geo location
     */
    GeoLocationResponse locate(Double latitude, Double longitude);


    /**
     * Locate geo location response.
     *
     * @param ip      the ip
     * @param request the request
     * @return the geo location response
     */
    GeoLocationResponse locate(String ip, GeoLocationRequest request);
}
