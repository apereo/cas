package org.apereo.cas.ticket.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.Table;
import java.util.Map;

/**
 * This is {@link SamlAttributeQueryTicketImpl}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "SAML2_ATTRIBUTE_QUERY_TICKETS")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(SamlAttributeQueryTicket.PREFIX)
public class SamlAttributeQueryTicketImpl extends AbstractTicket implements SamlAttributeQueryTicket {

    private static final long serialVersionUID = 6276140828446447398L;

    @Column(length = 255, updatable = true, insertable = true)
    private String issuer;

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

    @ElementCollection
    @CollectionTable(name = "SamlAttributeQueryTicket_Attributes")
    @MapKey(name = "key")
    @Column(name = "value")
    private Map<String, Object> attributes;

    /**
     * Instantiates a new OAuth code impl.
     */
    public SamlAttributeQueryTicketImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new OAuth code with unique id for a service and authentication.
     *
     * @param id               the unique identifier for the ticket.
     * @param service          the service this ticket is for.
     * @param expirationPolicy the expiration policy.
     * @param issuer           the issuer
     * @param attributes       the attributes
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public SamlAttributeQueryTicketImpl(final String id, final Service service,
                                        final ExpirationPolicy expirationPolicy,
                                        final String issuer, final Map<String, Object> attributes) {
        super(id, expirationPolicy);
        this.service = service;
        this.issuer = issuer;
        this.attributes = attributes;
    }

    @Override
    public String getIssuer() {
        return this.issuer;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }
    
    public void setService(final Service service) {
        this.service = service;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
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
        return this.ticketGrantingTicket.getAuthentication();
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public String getPrefix() {
        return SamlAttributeQueryTicket.PREFIX;
    }
}
