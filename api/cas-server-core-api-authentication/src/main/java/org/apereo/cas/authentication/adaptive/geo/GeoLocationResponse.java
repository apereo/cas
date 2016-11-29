package org.apereo.cas.authentication.adaptive.geo;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * This is {@link GeoLocationResponse} that represents a particular geo location
 * usually calculated from an ip address.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GeoLocationResponse {
    private Set<String> addresses = new ConcurrentSkipListSet<>();

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
    public String buildAddress() {
        return this.addresses.stream().collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("addresses", this.addresses)
                .toString();
    }
}
