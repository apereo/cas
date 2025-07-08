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
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
     * Field name for the timestamp of the event.
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * Field name for the unique identifier of the event.
     */
    public static final String FIELD_EVENT_ID = "eventId";

    /**
     * Field name for the client IP address associated with the event.
     */
    public static final String FIELD_CLIENT_IP = "clientip";

    /**
     * Field name for the server IP address associated with the event.
     */
    public static final String FIELD_SERVER_IP = "serverip";

    /**
     * Field name for the user agent or device information.
     */
    public static final String FIELD_AGENT = "agent";

    /**
     * Field name for the geographical latitude of the event's location.
     */
    public static final String FIELD_GEO_LATITUDE = "geoLatitude";

    /**
     * Field name for the geographical longitude of the event's location.
     */
    public static final String FIELD_GEO_LONGITUDE = "geoLongitude";

    /**
     * Field name for the accuracy of the geographical location.
     */
    public static final String FIELD_GEO_ACCURACY = "geoAccuracy";

    /**
     * Field name for the timestamp of the geographical location data.
     */
    public static final String FIELD_GEO_TIMESTAMP = "geoTimestamp";

    /**
     * Field name for the tenant or organization associated with the event.
     */
    public static final String FIELD_TENANT = "tenant";

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
    private Instant creationTime;

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

    /**
     * Put tenant.
     *
     * @param tenant the tenant
     * @return the cas event
     */
    public CasEvent putTenant(final String tenant) {
        return put(FIELD_TENANT, tenant);
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

    @JsonIgnore
    public String getTenant() {
        return get(FIELD_TENANT);
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

    /**
     * As new entity event.
     *
     * @return the cas event
     */
    @CanIgnoreReturnValue
    public CasEvent asNewEntity() {
        setId(0L);
        return this;
    }

    /**
     * From cas event to a new event.
     *
     * @param event the event
     * @return the cas event
     */
    public static CasEvent from(final CasEvent event) {
        val newEvent = new CasEvent();
        newEvent.setId(event.getId());
        newEvent.setType(event.getType());
        newEvent.setPrincipalId(event.getPrincipalId());
        newEvent.setCreationTime(event.getCreationTime());
        newEvent.setProperties(new LinkedHashMap<>(event.getProperties()));
        return newEvent;
    }
}
