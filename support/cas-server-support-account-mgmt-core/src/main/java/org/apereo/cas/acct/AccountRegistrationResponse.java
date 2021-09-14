package org.apereo.cas.acct;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AccountRegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@NoArgsConstructor
public class AccountRegistrationResponse implements Serializable {
    private static final long serialVersionUID = -1822843820128948428L;

    @Getter
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public AccountRegistrationResponse(final Map<String, Object> properties) {
        this.properties.putAll(properties);
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
        return clazz.cast(properties.get(name));
    }

    /**
     * Contains property.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Put property.
     *
     * @param name  the name
     * @param value the value
     */
    public void putProperty(final String name, final Object value) {
        this.properties.put(name, value);
    }

    /**
     * Put properties.
     *
     * @param map the as map
     */
    public void putProperties(final Map<String, Object> map) {
        this.properties.putAll(map);
    }
}
