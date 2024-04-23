package org.apereo.cas.ticket.artifact;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * This is {@link SamlArtifactTicketImpl}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@NoArgsConstructor
public class SamlArtifactTicketImpl extends AbstractTicket implements SamlArtifactTicket {

    @Serial
    private static final long serialVersionUID = 6276140828446447398L;

    private String issuer;

    private String relyingPartyId;

    private String object;

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The service this ticket is valid for.
     */
    private Service service;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    private Authentication authentication;

    public SamlArtifactTicketImpl(final String id, final Service service, final Authentication authentication,
                                  final ExpirationPolicy expirationPolicy,
                                  final TicketGrantingTicket ticketGrantingTicket,
                                  final String issuer, final String relyingParty, final String samlObject) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.relyingPartyId = relyingParty;
        this.issuer = issuer;
        this.object = samlObject;
    }

    @Override
    public String getPrefix() {
        return SamlArtifactTicket.PREFIX;
    }
}
