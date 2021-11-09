package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasSimpleMultifactorAuthenticationTicketImpl}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class CasSimpleMultifactorAuthenticationTicketImpl extends AbstractTicket implements CasSimpleMultifactorAuthenticationTicket {
    private static final long serialVersionUID = -6580305495605099699L;

    /**
     * The Service.
     */
    private Service service;

    /**
     * The Properties.
     */
    private Map<String, Object> properties = new HashMap<>(0);

    public CasSimpleMultifactorAuthenticationTicketImpl(final String id, final ExpirationPolicy expirationPolicy,
                                                        final Service service, final Map<String, Serializable> properties) {
        super(id, expirationPolicy);
        this.service = service;
        this.properties = new HashMap<>(properties);
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
