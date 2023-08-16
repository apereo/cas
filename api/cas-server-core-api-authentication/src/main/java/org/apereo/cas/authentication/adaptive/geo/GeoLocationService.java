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
     * Bean name.
     */
    String BEAN_NAME = "geoLocationService";

    /**
     * Find a geo location based on an address.
     *
     * @param address the address
     * @return the geo location
     * @throws Throwable the throwable
     */
    GeoLocationResponse locate(InetAddress address) throws Throwable;

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
     * @throws Throwable the throwable
     */
    GeoLocationResponse locate(Double latitude, Double longitude) throws Throwable;

    /**
     * Locate geo location response.
     *
     * @param ip      the ip
     * @param request the request
     * @return the geo location response
     * @throws Throwable the throwable
     */
    GeoLocationResponse locate(String ip, GeoLocationRequest request) throws Throwable;

    /**
     * Locate geo location response.
     *
     * @param request the request
     * @return the geo location response
     * @throws Throwable the throwable
     */
    GeoLocationResponse locate(GeoLocationRequest request) throws Throwable;
}
