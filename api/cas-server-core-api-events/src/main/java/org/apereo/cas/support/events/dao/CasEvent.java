package org.apereo.cas.support.events.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;

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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasEvent}, which represents a single event stored in the events repository.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "CasEvent")
public class CasEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = Integer.MAX_VALUE;

    @Column(updatable = true, insertable = true, nullable = false)
    private String type;

    @Column(updatable = true, insertable = true, nullable = false)
    private String principalId;

    @ElementCollection
    @MapKeyColumn(name="name")
    @Column(name="value")
    @CollectionTable(name="events_properties", joinColumns=@JoinColumn(name="id"))
    private Map<String, String> properties = new HashMap<>();

    public void setType(final String type) {
        this.type = type;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public String getType() {
        return this.type;
    }

    public Map<String, String> getProperties() {
        return this.properties;
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
     * Put creation time.
     *
     * @param time the time
     */
    public void putCreationTime(final ZonedDateTime time) {
        put("creationTime", time.toString());
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

    public ZonedDateTime getCreationTime() {
        return ZonedDateTime.parse(get("creationTime"));
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

    public String getPrincipalId() {
        return this.principalId;
    }

    public void setPrincipalId(final String principalId) {
        this.principalId = principalId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", this.type)
                .append("principalId", this.principalId)
                .toString();
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
}
