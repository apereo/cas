package org.jasig.cas.support.events.dao;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasEventDTO}, which represents a single event stored in the events repository.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class CasEventDTO {
    private String type;

    private Map<String, String> properties = new HashMap<>();

    public void setType(final String type) {
        this.type = type;
    }

    public void setProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getProperties() {
        return properties;
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
     * Put location.
     *
     * @param loc the loc
     */
    public void putLocation(final String loc) {
        put("location", loc);
    }

    /**
     * Put agent.
     *
     * @param dev the dev
     */
    public void putAgent(final String dev) {
        put("agent", dev);
    }

    /**
     * Put property.
     *
     * @param key   the key
     * @param value the value
     */
    public void put(final String key, final String value) {
        this.properties.put(key, value);
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


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .toString();
    }
}
