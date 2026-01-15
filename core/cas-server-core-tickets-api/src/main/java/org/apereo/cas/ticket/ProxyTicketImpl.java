package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.OptBoolean;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The {@link ProxyTicketImpl} is a concrete implementation of the {@link ProxyTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor
@Accessors(chain = true)
public class ProxyTicketImpl extends ServiceTicketImpl implements ProxyTicket {

    @Serial
    private static final long serialVersionUID = -4469960563289285371L;

    @JsonCreator
    public ProxyTicketImpl(@JsonProperty("id") final String id,
                           @JsonProperty("ticketGrantingTicket") final TicketGrantingTicket ticket,
                           @JsonProperty("service") final Service service,
                           @JsonProperty(value = "credentialProvided", isRequired = OptBoolean.FALSE)
                           @JsonSetter(nulls = Nulls.SKIP)
                           final boolean credentialProvided,
                           @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {
        super(id, ticket, service, credentialProvided, policy);
    }

    @Override
    public String getPrefix() {
        return ProxyTicket.PROXY_TICKET_PREFIX;
    }
}
