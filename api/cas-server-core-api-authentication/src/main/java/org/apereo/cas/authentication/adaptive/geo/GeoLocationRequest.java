package org.apereo.cas.authentication.adaptive.geo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is {@link GeoLocationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GeoLocationRequest {
    private String latitude;
    private String longitude;
    private String accuracy;
    private String timestamp;

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(final String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(final String longitude) {
        this.longitude = longitude;
    }

    public String getAccuracy() {
        return this.accuracy;
    }

    public void setAccuracy(final String accuracy) {
        this.accuracy = accuracy;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.latitude) && StringUtils.isNotBlank(this.longitude)
                && StringUtils.isNotBlank(this.accuracy) && StringUtils.isNotBlank(this.timestamp);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("latitude", latitude)
                .append("longitude", longitude)
                .append("accuracy", accuracy)
                .append("timestamp", timestamp)
                .toString();
    }
}
