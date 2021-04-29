package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;

/**
 * This is {@link DefaultSecurityTokenTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class DefaultSecurityTokenTicket extends AbstractTicket implements SecurityTokenTicket {

    private static final long serialVersionUID = 3940671352560102114L;

    @Getter
    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

    @JsonProperty
    private String securityToken;
    
    public DefaultSecurityTokenTicket(final String id, final TicketGrantingTicket ticketGrantingTicket,
                                      final ExpirationPolicy expirationPolicy, final String securityToken) {
        super(id, expirationPolicy);
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.securityToken = securityToken;
    }

    @Override
    public Authentication getAuthentication() {
        return getTicketGrantingTicket().getAuthentication();
    }

    @Override
    public String getPrefix() {
        return SecurityTokenTicket.PREFIX;
    }

    @Override
    @JsonIgnore
    public SecurityToken getSecurityToken() {
        val securityTokenBin = EncodingUtils.decodeBase64(this.securityToken);
        return SerializationUtils.deserialize(securityTokenBin);
    }
}
