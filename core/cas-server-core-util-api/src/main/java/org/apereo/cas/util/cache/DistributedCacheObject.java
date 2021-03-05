package org.apereo.cas.util.cache;

import org.apereo.cas.util.PublisherIdentifier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link DistributedCacheObject}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@ToString
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class DistributedCacheObject<V extends Serializable> implements Serializable {
    private static final long serialVersionUID = -6776499291439952013L;

    @Builder.Default
    private Map<String, String> properties = new TreeMap<>();

    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    private V value;

    private PublisherIdentifier publisherIdentifier;

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
                throw new ClassCastException("Object [" + item + " is of type "
                    + item.getClass() + " when we were expecting " + clazz);
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
