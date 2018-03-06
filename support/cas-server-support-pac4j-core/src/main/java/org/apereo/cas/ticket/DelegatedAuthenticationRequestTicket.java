package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Service;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationRequestTicket}, issued when a delegated authentication
 * request comes in that needs to be handed off to an identity provider. This ticket represents the state
 * of the CAS server at that moment.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Table(name = "DELEGATEDAUTHENTICATIONREQUESTTICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(DelegatedAuthenticationRequestTicket.PREFIX)
public class DelegatedAuthenticationRequestTicket extends AbstractTicket {
    /**
     * Ticket prefix for the delegated authentication request.
     */
    public static final String PREFIX = "DART";

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 7839186396717950243L;

    /**
     * The Service.
     */
    @Lob
    @Column(name = "SERVICE", nullable = false)
    private Service service;

    /**
     * The Properties.
     */
    @Lob
    @Column(name = "PROPERTIES", nullable = false)
    private Map<String, Object> properties = new LinkedHashMap<>();

    public DelegatedAuthenticationRequestTicket(final String id, final ExpirationPolicy expirationPolicy, final Service service) {
        super(id, expirationPolicy);
        this.service = service;
    }

    public DelegatedAuthenticationRequestTicket(final String id, final ExpirationPolicy expirationPolicy,
                                                final Service service, final Map<String, Object> properties) {
        super(id, expirationPolicy);
        this.service = service;
        this.properties = properties;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /**
     * Put property.
     *
     * @param name  the name
     * @param value the value
     */
    public void put(final String name, final Object value) {
        this.properties.put(name, value);
    }

    /**
     * Put all properties.
     *
     * @param props the props
     */
    public void putAll(final Map<String, Object> props) {
        this.properties.putAll(props);
    }

    /**
     * Contains property boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean contains(final String name) {
        return this.properties.containsKey(name);
    }

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the property
     */
    public <T> T get(final String name, final Class<T> clazz) {
        if (contains(name)) {
            return clazz.cast(this.properties.get(name));
        }
        return null;
    }

    /**
     * Gets property.
     *
     * @param <T>          the type parameter
     * @param name         the name
     * @param clazz        the clazz
     * @param defaultValue the default value
     * @return the property
     */
    public <T> T get(final String name, final Class<T> clazz, final T defaultValue) {
        if (contains(name)) {
            return clazz.cast(this.properties.getOrDefault(name, defaultValue));
        }
        return null;
    }
}

