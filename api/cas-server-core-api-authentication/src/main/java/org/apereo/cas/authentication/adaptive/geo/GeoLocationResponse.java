package org.apereo.cas.authentication.adaptive.geo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This is {@link GeoLocationResponse} that represents a particular geo location
 * usually calculated from an ip address.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
     * @return the geo location response
     */
    public GeoLocationResponse addAddress(final String address) {
        if (StringUtils.isNotBlank(address)) {
            this.addresses.add(address);
        }
        return this;
    }

    /**
     * Format the address into a long string.
     *
     * @return the string
     */
    public String build() {
        return String.join(",", this.addresses);
    }
}
