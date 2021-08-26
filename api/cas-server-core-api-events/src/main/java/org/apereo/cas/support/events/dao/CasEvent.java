package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
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

    private static final long serialVersionUID = -4206712375316470417L;

    @Id
    @JsonProperty
    @Transient
    private long id = -1;

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
    @Column(name = "value")
    @CollectionTable(name = "events_properties", joinColumns = @JoinColumn(name = "eventId"))
    private Map<String, String> properties = new HashMap<>(0);

    /**
     * Instantiates a new CAS event.
     */
    public CasEvent() {
        this.id = System.currentTimeMillis();
    }

    /**
     * Put timestamp.
     *
     * @param time the time
     */
    public void putTimestamp(final Long time) {
        put(FIELD_TIMESTAMP, time.toString());
    }

    /**
     * Put id.
     *
     * @param eventId the id
     */
    public void putEventId(final String eventId) {
        put(FIELD_EVENT_ID, eventId);
    }

    /**
     * Put client ip.
     *
     * @param loc the loc
     */
    public void putClientIpAddress(final String loc) {
        put(FIELD_CLIENT_IP, loc);
    }

    /**
     * Put server ip.
     *
     * @param loc the loc
     */
    public void putServerIpAddress(final String loc) {
        put(FIELD_SERVER_IP, loc);
    }

    /**
     * Put agent.
     *
     * @param dev the dev
     */
    public void putAgent(final String dev) {
        put(FIELD_AGENT, dev);
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
     */
    public void put(final String key, final String value) {
        if (StringUtils.isBlank(value)) {
            this.properties.remove(key);
        } else {
            this.properties.put(key, value);
        }
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
     */
    public void putGeoLocation(final GeoLocationRequest location) {
        putGeoAccuracy(location.getAccuracy());
        putGeoLatitude(location.getLatitude());
        putGeoLongitude(location.getLongitude());
        putGeoTimestamp(location.getTimestamp());
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

    /**
     * Put geo latitude.
     *
     * @param s the s
     */
    private void putGeoLatitude(final String s) {
        put(FIELD_GEO_LATITUDE, s);
    }

    /**
     * Put geo longitude.
     *
     * @param s the longitude
     */
    private void putGeoLongitude(final String s) {
        put(FIELD_GEO_LONGITUDE, s);
    }

    /**
     * Put geo accuracy.
     *
     * @param s the accuracy
     */
    private void putGeoAccuracy(final String s) {
        put(FIELD_GEO_ACCURACY, s);
    }

    /**
     * Put geo timestamp.
     *
     * @param s the timestamp
     */
    private void putGeoTimestamp(final String s) {
        put(FIELD_GEO_TIMESTAMP, s);
    }
}
