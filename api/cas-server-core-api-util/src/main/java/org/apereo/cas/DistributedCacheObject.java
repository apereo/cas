package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.ToString;
import lombok.Getter;

/**
 * This is {@link DistributedCacheObject}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@ToString
@Getter
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
