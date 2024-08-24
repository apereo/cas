package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasEvent}, which represents a single event stored in the events repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@MappedSuperclass
@ToString
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
public class CasEvent implements Serializable {

    /**
     * The constant FIELD_TIMESTAMP.
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * The constant FIELD_EVENT_ID.
     */
    public static final String FIELD_EVENT_ID = "eventId";

    /**
     * The constant FIELD_CLIENTIP.
     */
    public static final String FIELD_CLIENT_IP = "clientip";

    /**
     * The constant FIELD_SERVERIP.
     */
    public static final String FIELD_SERVER_IP = "serverip";

    /**
     * The constant FIELD_AGENT.
     */
    public static final String FIELD_AGENT = "agent";

    /**
     * The constant FIELD_GEO_LATITUDE.
     */
    public static final String FIELD_GEO_LATITUDE = "geoLatitude";

    /**
     * The constant FIELD_GEO_LONGITUDE.
     */
    public static final String FIELD_GEO_LONGITUDE = "geoLongitude";

    /**
     * The constant FIELD_GEO_ACCURACY.
     */
    public static final String FIELD_GEO_ACCURACY = "geoAccuracy";

    /**
     * The constant FIELD_GEO_TIMESTAMP.
     */
    public static final String FIELD_GEO_TIMESTAMP = "geoTimestamp";

    @Serial
    private static final long serialVersionUID = -4206712375316470417L;

    private static final String FIELD_DEVICE_FINGERPRINT = "deviceFingerprint";

    @Id
    @JsonProperty
    @Transient
    private long id;

    @JsonProperty("type")
    @Column(nullable = false)
    private String type;

    @JsonProperty("principalId")
    @Column(nullable = false)
    private String principalId;

    @JsonProperty("creationTime")
    @Column(nullable = false)
    private String creationTime;

    @JsonProperty("properties")
    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value", length = 4_000)
    @CollectionTable(name = "events_properties", joinColumns = @JoinColumn(name = "eventId"))
    private Map<String, String> properties = new HashMap<>();

    /**
     * Put timestamp.
     *
     * @param time the time
     * @return the cas event
     */
    public CasEvent putTimestamp(final Long time) {
        return put(FIELD_TIMESTAMP, time.toString());
    }

    /**
     * Put id.
     *
     * @param eventId the id
     * @return the cas event
     */
    public CasEvent putEventId(final String eventId) {
        return put(FIELD_EVENT_ID, eventId);
    }

    /**
     * Put client ip.
     *
     * @param loc the loc
     * @return the cas event
     */
    public CasEvent putClientIpAddress(final String loc) {
        return put(FIELD_CLIENT_IP, loc);
    }

    /**
     * Put server ip.
     *
     * @param loc the loc
     * @return the cas event
     */
    public CasEvent putServerIpAddress(final String loc) {
        return put(FIELD_SERVER_IP, loc);
    }

    /**
     * Put agent.
     *
     * @param dev the dev
     * @return the cas event
     */
    public CasEvent putAgent(final String dev) {
        return put(FIELD_AGENT, dev);
    }

    @JsonIgnore
    public Long getTimestamp() {
        return Long.valueOf(get(FIELD_TIMESTAMP));
    }

    @JsonIgnore
    public String getAgent() {
        return get(FIELD_AGENT);
    }

    @JsonIgnore
    public String getEventId() {
        return get(FIELD_EVENT_ID);
    }

    @JsonIgnore
    public String getClientIpAddress() {
        return get(FIELD_CLIENT_IP);
    }

    @JsonIgnore
    public String getServerIpAddress() {
        return get(FIELD_SERVER_IP);
    }

    /**
     * Put property.
     *
     * @param key   the key
     * @param value the value
     * @return the cas event
     */
    @CanIgnoreReturnValue
    public CasEvent put(final String key, final String value) {
        if (StringUtils.isBlank(value)) {
            this.properties.remove(key);
        } else {
            this.properties.put(key, value);
        }
        return this;
    }

    /**
     * Get property.
     *
     * @param key the key
     * @return the string
     */
    public String get(final String key) {
        return this.properties.get(key);
    }

    /**
     * Put geo location.
     *
     * @param location the location
     * @return the cas event
     */
    @CanIgnoreReturnValue
    public CasEvent putGeoLocation(final GeoLocationRequest location) {
        putGeoAccuracy(location.getAccuracy());
        putGeoLatitude(location.getLatitude());
        putGeoLongitude(location.getLongitude());
        putGeoTimestamp(location.getTimestamp());
        return this;
    }

    /**
     * Put device fingerprint into cas event.
     *
     * @param value the value
     * @return the cas event
     */
    public CasEvent putDeviceFingerprint(final String value) {
        return put(FIELD_DEVICE_FINGERPRINT, value);
    }

    /**
     * Gets device fingerprint.
     *
     * @return the device fingerprint
     */
    public String getDeviceFingerprint() {
        return get(FIELD_DEVICE_FINGERPRINT);
    }

    /**
     * Gets geo location.
     *
     * @return the geo location
     */
    @JsonIgnore
    public GeoLocationRequest getGeoLocation() {
        val request = new GeoLocationRequest();
        request.setAccuracy(get(FIELD_GEO_ACCURACY));
        request.setTimestamp(get(FIELD_GEO_TIMESTAMP));
        request.setLongitude(get(FIELD_GEO_LONGITUDE));
        request.setLatitude(get(FIELD_GEO_LATITUDE));
        return request;
    }
    
    private CasEvent putGeoLatitude(final String s) {
        return put(FIELD_GEO_LATITUDE, s);
    }
    
    private CasEvent putGeoLongitude(final String s) {
        return put(FIELD_GEO_LONGITUDE, s);
    }

    private CasEvent putGeoAccuracy(final String s) {
        return put(FIELD_GEO_ACCURACY, s);
    }

    private CasEvent putGeoTimestamp(final String s) {
        return put(FIELD_GEO_TIMESTAMP, s);
    }

    /**
     * Assign id if undefined.
     *
     * @return the registered service
     */
    @CanIgnoreReturnValue
    public CasEvent assignIdIfNecessary() {
        if (getId() <= 0) {
            setId(System.currentTimeMillis());
        }
        return this;
    }
}
