package org.apereo.cas;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DistributedCacheObject}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DistributedCacheObject<V extends Serializable> implements Serializable {
    private static final long serialVersionUID = -6776499291439952013L;

    private Map<String, Object> properties = new LinkedHashMap<>();
    private final long timestamp;
    private final V value;

    public DistributedCacheObject(final V value) {
        this(new Date().getTime(), value);
    }

    public DistributedCacheObject(final long timestamp, final V value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("timestamp", timestamp)
            .append("value", value)
            .append("properties", properties)
            .toString();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the property
     */
    public <T> T getProperty(final String name, final Class<T> clazz) {
        if (containsProperty(name)) {
            final Object item = this.properties.get(name);
            if (item == null) {
                return null;
            }

            if (!clazz.isAssignableFrom(item.getClass())) {
                throw new ClassCastException("Object [" + item + " is of type " + item.getClass() + " when we were expecting " + clazz);
            }
            return (T) item;
        }
        return null;
    }

    /**
     * Contains property?
     *
     * @param name the name
     * @return the boolean
     */
    public boolean containsProperty(final String name) {
        return this.properties.containsKey(name);
    }
}
