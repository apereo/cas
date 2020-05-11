package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link TransientSessionTicket} that allows CAS to use the ticket registry
 * as a distributed session store for short-lived non-specific objects. The intention
 * of this ticket is to encapsulate objects that would otherwise be tracked by the application
 * container's session. By using the ticket registry as a generic session store, all operations
 * that may require session awareness specially in a clustered environment can be freed from
 * that requirement.
 * <p>
 * Note that objects/values put into the session ticket are required to be serializable,
 * just as normal ticket properties would be, depending on the design of the underlying ticket registry.
 * <p>
 * Transient tickets generally have prominent use when CAS is acting as a proxy to another identity provider
 * where the results of current application session/request need to be stored across the cluster and remembered later.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TransientSessionTicket extends Ticket {
    /**
     * Ticket prefix for the delegated authentication request.
     */
    String PREFIX = "TST";

    /**
     * Gets properties.
     *
     * @return the properties
     */
    Map<String, Object> getProperties();

    /**
     * Gets service.
     *
     * @return the service
     */
    Service getService();

    /**
     * Put property.
     *
     * @param name  the name
     * @param value the value
     */
    void put(String name, Serializable value);

    /**
     * Put all properties.
     *
     * @param props the props
     */
    void putAll(Map<String, Serializable> props);

    /**
     * Contains property boolean.
     *
     * @param name the name
     * @return true/false
     */
    boolean contains(String name);

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the property
     */
    <T extends Serializable> T get(String name, Class<T> clazz);

    /**
     * Gets property.
     *
     * @param <T>          the type parameter
     * @param name         the name
     * @param clazz        the clazz
     * @param defaultValue the default value
     * @return the property
     */
    <T extends Serializable> T get(String name, Class<T> clazz, T defaultValue);
}
