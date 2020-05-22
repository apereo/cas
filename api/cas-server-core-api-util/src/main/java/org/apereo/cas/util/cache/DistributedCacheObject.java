package org.apereo.cas.util.cache;

import lombok.Getter;
import lombok.ToString;
import lombok.val;

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
@ToString
@Getter
public class DistributedCacheObject<V extends Serializable> implements Serializable {
    private static final int MAP_SIZE = 8;
    private static final long serialVersionUID = -6776499291439952013L;
    private final long timestamp;
    private final V value;
    private final Map<String, Object> properties = new LinkedHashMap<>(MAP_SIZE);

    public DistributedCacheObject(final V value) {
        this(new Date().getTime(), value);
    }

    public DistributedCacheObject(final long timestamp, final V value) {
        this.timestamp = timestamp;
        this.value = value;
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
            val item = this.properties.get(name);
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
     * @return true/false
     */
    public boolean containsProperty(final String name) {
        return this.properties.containsKey(name);
    }
}
