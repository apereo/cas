package org.apereo.cas.support.events.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.util.DateTimeUtils;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CasEvent}, which represents a single event stored in the events repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "CasEvent")
@Slf4j
@ToString
@Getter
@Setter
public class CasEvent {

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String principalId;

    @Column(nullable = false)
    private String creationTime;

    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "events_properties", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> properties = new HashMap<>();

    /**
     * Instantiates a new Cas event.
     */
    public CasEvent() {
        this.id = System.currentTimeMillis();
    }

    /**
     * Gets creation time. Attempts to parse the value
     * as a {@link ZonedDateTime}. Otherwise, assumes a
     * {@link LocalDateTime} and converts it based on system's
     * default zone.
     *
     * @return the creation time
     */
    public ZonedDateTime getCreationTime() {
        final ZonedDateTime dt = DateTimeUtils.zonedDateTimeOf(this.creationTime);
        if (dt != null) {
            return dt;
        }
        final LocalDateTime lt = DateTimeUtils.localDateTimeOf(this.creationTime);
        return DateTimeUtils.zonedDateTimeOf(lt.atZone(ZoneId.systemDefault()));
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
     * @param id the id
     */
    public void putId(final String id) {
        put("id", id);
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

    public Long getTimestamp() {
        return Long.valueOf(get("timestamp"));
    }

    public String getAgent() {
        return get("agent");
    }

    public String getId() {
        return get("id");
    }

    public String getClientIpAddress() {
        return get("clientip");
    }

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
    public GeoLocationRequest getGeoLocation() {
        final GeoLocationRequest request = new GeoLocationRequest();
        request.setAccuracy(get("geoAccuracy"));
        request.setTimestamp(get("geoTimestamp"));
        request.setLongitude(get("geoLongitude"));
        request.setLatitude(get("geoLatitude"));
        return request;
    }
}
