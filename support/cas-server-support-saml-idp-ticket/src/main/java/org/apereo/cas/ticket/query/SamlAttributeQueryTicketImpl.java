package org.apereo.cas.ticket.query;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is {@link SamlAttributeQueryTicketImpl}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@NoArgsConstructor
@Setter
public class SamlAttributeQueryTicketImpl extends AbstractTicket implements SamlAttributeQueryTicket {

    private static final long serialVersionUID = 6276140828446447398L;

    private String relyingParty;

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
     * Constructs saml attribute query ticket.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param expirationPolicy     the expiration policy.
     * @param relyingParty         the relying party
     * @param samlObject           the saml object
     * @param ticketGrantingTicket the ticket granting ticket
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public SamlAttributeQueryTicketImpl(final String id, final Service service, final ExpirationPolicy expirationPolicy,
                                        final String relyingParty, final String samlObject, final TicketGrantingTicket ticketGrantingTicket) {
        super(id, expirationPolicy);
        this.service = service;
        this.relyingParty = relyingParty;
        this.object = samlObject;
        this.ticketGrantingTicket = ticketGrantingTicket;
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No proxy granting ticket is available");
    }

    @Override
    public Authentication getAuthentication() {
        return this.ticketGrantingTicket.getAuthentication();
    }

    @Override
    public String getPrefix() {
        return SamlAttributeQueryTicket.PREFIX;
    }
}
