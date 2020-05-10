package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link TransientSessionTicketImpl}, issued when a delegated authentication
 * request comes in that needs to be handed off to an identity provider. This ticket represents the state
 * of the CAS server at that moment.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Table(name = "TRANSIENTSESSIONTICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(TransientSessionTicket.PREFIX)
public class TransientSessionTicketImpl extends AbstractTicket implements TransientSessionTicket {
    private static final long serialVersionUID = 7839186396717950243L;

    /**
     * The Service.
     */
    @Lob
    @Column(name = "SERVICE", length = Integer.MAX_VALUE)
    private Service service;

    /**
     * The Properties.
     */
    @Lob
    @Column(name = "PROPERTIES", length = Integer.MAX_VALUE, nullable = false)
    private HashMap<String, Object> properties = new HashMap<>(0);

    public TransientSessionTicketImpl(final String id, final ExpirationPolicy expirationPolicy, final Service service) {
        super(id, expirationPolicy);
        this.service = service;
    }

    public TransientSessionTicketImpl(final String id, final ExpirationPolicy expirationPolicy,
                                      final Service service, final Map<String, Serializable> properties) {
        super(id, expirationPolicy);
        this.service = service;
        this.properties = new HashMap<>(properties);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public void put(final String name, final Serializable value) {
        this.properties.put(name, value);
    }

    @Override
    public void putAll(final Map<String, Serializable> props) {
        this.properties.putAll(props);
    }

    @Override
    public boolean contains(final String name) {
        return this.properties.containsKey(name);
    }

    @Override
    public <T extends Serializable> T get(final String name, final Class<T> clazz) {
        if (contains(name)) {
            return clazz.cast(this.properties.get(name));
        }
        return null;
    }

    @Override
    public <T extends Serializable> T get(final String name, final Class<T> clazz, final T defaultValue) {
        if (contains(name)) {
            return clazz.cast(this.properties.getOrDefault(name, defaultValue));
        }
        return defaultValue;
    }
}

