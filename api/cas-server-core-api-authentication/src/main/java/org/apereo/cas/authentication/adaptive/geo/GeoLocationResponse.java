package org.apereo.cas.authentication.adaptive.geo;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
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
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
public class GeoLocationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -4380882448842426005L;

    private final Set<String> addresses = new ConcurrentSkipListSet<>();

    private double latitude;

    private double longitude;

    /**
     * Add address.
     *
     * @param address the address
     * @return the geo location response
     */
    @CanIgnoreReturnValue
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
