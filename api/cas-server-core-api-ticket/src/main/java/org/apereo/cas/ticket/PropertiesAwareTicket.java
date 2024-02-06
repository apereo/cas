package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link PropertiesAwareTicket}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface PropertiesAwareTicket extends Ticket {
    /**
     * Gets properties.
     *
     * @return the properties
     */
    Map<String, Object> getProperties();

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @return the property
     */
    <T> T getProperty(String key, Class<T> clazz);

    /**
     * Gets property.
     *
     * @param <T>          the type parameter
     * @param name         the name
     * @param clazz        the clazz
     * @param defaultValue the default value
     * @return the property
     */
    <T extends Serializable> T getProperty(String name, Class<T> clazz, T defaultValue);
    
    /**
     * Put property.
     *
     * @param name  the name
     * @param value the value
     */
    void putProperty(String name, Serializable value);

    /**
     * Put all properties.
     *
     * @param props the props
     */
    void putAllProperties(Map<String, Serializable> props);

    /**
     * Contains property boolean.
     *
     * @param name the name
     * @return true/false
     */
    boolean containsProperty(String name);

}
