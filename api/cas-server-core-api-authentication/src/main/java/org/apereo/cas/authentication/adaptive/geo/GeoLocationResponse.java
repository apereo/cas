package org.apereo.cas.authentication.adaptive.geo;

import lombok.extern.slf4j.Slf4j;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link GeoLocationResponse} that represents a particular geo location
 * usually calculated from an ip address.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Getter
@Setter
public class GeoLocationResponse {

    private final Set<String> addresses = new ConcurrentSkipListSet<>();

    private double latitude;

    private double longitude;

    /**
     * Add address.
     *
     * @param address the address
     */
    public void addAddress(final String address) {
        this.addresses.add(address);
    }

    /**
     * Format the address into a long string.
     *
     * @return the string
     */
    public String build() {
        return this.addresses.stream().collect(Collectors.joining(","));
    }
}
