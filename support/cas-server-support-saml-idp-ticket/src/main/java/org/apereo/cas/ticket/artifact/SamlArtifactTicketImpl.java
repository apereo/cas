package org.apereo.cas.ticket.artifact;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This is {@link SamlArtifactTicketImpl}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "SAML2_ARTIFACTS")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(SamlArtifactTicket.PREFIX)
@Getter
@NoArgsConstructor
public class SamlArtifactTicketImpl extends AbstractTicket implements SamlArtifactTicket {

    private static final long serialVersionUID = 6276140828446447398L;

    @Column(length = 5_000)
    private String issuer;

    @Column(length = 5_000)
    private String relyingPartyId;

    @Lob
    @Column(length = 10_000)
    private String object;

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The service this ticket is valid for.
     */
    @Lob
    @Column(name = "SERVICE", nullable = false)
    private Service service;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    @Lob
    @Column(name = "AUTHENTICATION", nullable = false, length = Integer.MAX_VALUE)
    private Authentication authentication;


    /**
     * Constructs a new OAuth code with unique id for a service and authentication.
     *
     * @param id                   the unique identifier for the ticket.
     * @param service              the service this ticket is for.
     * @param authentication       the authentication.
     * @param expirationPolicy     the expiration policy.
     * @param ticketGrantingTicket the ticket granting ticket
     * @param issuer               the issuer
     * @param relyingParty         the relying party
     * @param samlObject           the saml object
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public SamlArtifactTicketImpl(final String id, final Service service, final Authentication authentication, final ExpirationPolicy expirationPolicy,
                                  final TicketGrantingTicket ticketGrantingTicket, final String issuer, final String relyingParty, final String samlObject) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.relyingPartyId = relyingParty;
        this.issuer = issuer;
        this.object = samlObject;
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No proxy-granting ticket is available");
    }

    @Override
    public String getPrefix() {
        return SamlArtifactTicket.PREFIX;
    }
}
