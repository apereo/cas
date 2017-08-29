package org.apereo.cas.ticket.artifact;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

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
public class SamlArtifactTicketImpl extends AbstractTicket implements SamlArtifactTicket {

    private static final long serialVersionUID = 6276140828446447398L;

    @Column(length = 500, updatable = true, insertable = true)
    private String issuer;

    @Column(length = 500, updatable = true, insertable = true)
    private String relyingParty;

    @Column(length = 5000, updatable = true, insertable = true)
    private String samlObject;

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    @JsonProperty("grantingTicket")
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
    @Column(name = "AUTHENTICATION", nullable = false, length = 1000000)
    private Authentication authentication;

    /**
     * Instantiates a new OAuth code impl.
     */
    public SamlArtifactTicketImpl() {
        // exists for JPA purposes
    }

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
    public SamlArtifactTicketImpl(final String id, final Service service, final Authentication authentication,
                                  final ExpirationPolicy expirationPolicy, final TicketGrantingTicket ticketGrantingTicket,
                                  final String issuer, final String relyingParty,
                                  final String samlObject) {
        super(id, expirationPolicy);
        this.service = service;
        this.authentication = authentication;
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.relyingParty = relyingParty;
        this.issuer = issuer;
        this.samlObject = samlObject;
    }

    @Override
    public String getIssuer() {
        return this.issuer;
    }

    @Override
    public String getRelyingPartyId() {
        return this.relyingParty;
    }

    @Override
    public String getObject() {
        return this.samlObject;
    }

    @Override
    public boolean isFromNewLogin() {
        return true;
    }

    @Override
    public Service getService() {
        return this.service;
    }

    @Override
    public boolean isValidFor(final Service serviceToValidate) {
        update();
        return serviceToValidate.matches(this.service);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(
            final String id, final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
        throw new UnsupportedOperationException("No PGT grant is available");
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public String getPrefix() {
        return SamlArtifactTicket.PREFIX;
    }
}
