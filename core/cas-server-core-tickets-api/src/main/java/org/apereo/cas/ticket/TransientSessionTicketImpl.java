package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class TransientSessionTicketImpl extends AbstractTicket implements TransientSessionTicket {
    @Serial
    private static final long serialVersionUID = 7839186396717950243L;

    /**
     * The Service.
     */
    private Service service;
    
    public TransientSessionTicketImpl(final String id, final ExpirationPolicy expirationPolicy,
                                      final Service service,
                                      @JsonSetter(nulls = Nulls.AS_EMPTY)
                                      final Map<String, Serializable> properties) {
        super(id, expirationPolicy);
        this.service = service;
        setProperties(new HashMap<>(properties));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}

