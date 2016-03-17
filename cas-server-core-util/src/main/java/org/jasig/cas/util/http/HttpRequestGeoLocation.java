package org.jasig.cas.util.http;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link HttpRequestGeoLocation}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class HttpRequestGeoLocation {
    private String latitude;
    private String longitude;
    private String accuracy;
    private String timestamp;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(final String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(final String longitude) {
        this.longitude = longitude;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(final String accuracy) {
        this.accuracy = accuracy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.latitude) && StringUtils.isNotBlank(this.longitude)
                && StringUtils.isNotBlank(this.accuracy) && StringUtils.isNotBlank(this.timestamp);
    }
}
