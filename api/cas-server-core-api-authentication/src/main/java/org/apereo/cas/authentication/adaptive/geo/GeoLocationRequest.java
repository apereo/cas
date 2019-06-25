package org.apereo.cas.authentication.adaptive.geo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link GeoLocationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Getter
@NoArgsConstructor
@Setter
@EqualsAndHashCode(exclude = {"accuracy", "timestamp"})
public class GeoLocationRequest implements Serializable {

    private static final long serialVersionUID = -3330957747025206526L;

    private String latitude;

    private String longitude;

    private String accuracy;

    private String timestamp;

    public GeoLocationRequest(final double latitude, final double longitude) {
        this.latitude = String.valueOf(latitude);
        this.longitude = String.valueOf(longitude);
    }

    /**
     * Check whether the geolocation contains enough data to proceed.
     *
     * @return true/false
     */
    public boolean isValid() {
        return StringUtils.isNotBlank(this.latitude) && StringUtils.isNotBlank(this.longitude)
            && StringUtils.isNotBlank(this.accuracy) && StringUtils.isNotBlank(this.timestamp);
    }
}
