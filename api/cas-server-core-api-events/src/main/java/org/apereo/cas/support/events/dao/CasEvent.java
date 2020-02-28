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
        put("timestamp", time.toString());
    }

    /**
     * Put id.
     *
     * @param eventId the id
     */
    public void putEventId(final String eventId) {
        put("eventId", eventId);
    }

    /**
     * Put client ip.
     *
     * @param loc the loc
     */
    public void putClientIpAddress(final String loc) {
        put("clientip", loc);
    }

    /**
     * Put server ip.
     *
     * @param loc the loc
     */
    public void putServerIpAddress(final String loc) {
        put("serverip", loc);
    }

    /**
     * Put agent.
     *
     * @param dev the dev
     */
    public void putAgent(final String dev) {
        put("agent", dev);
    }

    @JsonIgnore
    public Long getTimestamp() {
        return Long.valueOf(get("timestamp"));
    }

    @JsonIgnore
    public String getAgent() {
        return get("agent");
    }

    @JsonIgnore
    public String getEventId() {
        return get("eventId");
    }

    @JsonIgnore
    public String getClientIpAddress() {
        return get("clientip");
    }

    @JsonIgnore
    public String getServerIpAddress() {
        return get("serverip");
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
     * Put geo latitude.
     *
     * @param s the s
     */
    private void putGeoLatitude(final String s) {
        put("geoLatitude", s);
    }

    /**
     * Put geo longitude.
     *
     * @param s the longitude
     */
    private void putGeoLongitude(final String s) {
        put("geoLongitude", s);
    }

    /**
     * Put geo accuracy.
     *
     * @param s the accuracy
     */
    private void putGeoAccuracy(final String s) {
        put("geoAccuracy", s);
    }

    /**
     * Put geo timestamp.
     *
     * @param s the timestamp
     */
    private void putGeoTimestamp(final String s) {
        put("geoTimestamp", s);
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
        request.setAccuracy(get("geoAccuracy"));
        request.setTimestamp(get("geoTimestamp"));
        request.setLongitude(get("geoLongitude"));
        request.setLatitude(get("geoLatitude"));
        return request;
    }
}
