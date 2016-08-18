package org.apereo.cas.authentication.adaptive.geo;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * This is {@link GeoLocationResponse} that represents a particular geo location
 * usually calculated from an ip address.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GeoLocationResponse {
    private Set<String> addresses = Sets.newConcurrentHashSet();

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
        final StringBuilder b = new StringBuilder();
        this.addresses.forEach(s -> b.append(s.concat(",")));
        return b.toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("addresses", this.addresses)
                .toString();
    }
}
