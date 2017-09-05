package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of a TicketGrantingTicket. A TicketGrantingTicket is
 * the global identifier of a principal into the system. It grants the Principal
 * single-sign on access to any service that opts into single-sign on.
 * Expiration of a TicketGrantingTicket is controlled by the ExpirationPolicy
 * specified as object creation.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Entity
@Table(name = "TICKETGRANTINGTICKET")
@DiscriminatorColumn(name = "TYPE")
@DiscriminatorValue(TicketGrantingTicket.PREFIX)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class TicketGrantingTicketImpl extends AbstractTicket implements TicketGrantingTicket {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -8608149809180911599L;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    @Lob
    @Column(name = "AUTHENTICATION", nullable = false, length = Integer.MAX_VALUE)
    private Authentication authentication;

    /**
     * Flag to enforce manual expiration.
     */
    @Column(name = "EXPIRED", nullable = false)
    private Boolean expired = Boolean.FALSE;

    /**
     * Service that produced a proxy-granting ticket.
     */
    @Lob
    @Column(name = "PROXIED_BY", nullable = true, length = Integer.MAX_VALUE)
    private Service proxiedBy;

    /**
     * The services associated to this ticket.
     */
    @Lob
    @Column(name = "SERVICES_GRANTED_ACCESS_TO", nullable = false, length = Integer.MAX_VALUE)
    private HashMap<String, Service> services = new HashMap<>();

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    @ManyToOne(targetEntity = TicketGrantingTicketImpl.class)
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The PGTs associated to this ticket.
     */
    @Lob
    @Column(name = "PROXY_GRANTING_TICKETS", nullable = false, length = Integer.MAX_VALUE)
    private HashMap<String, Service> proxyGrantingTickets = new HashMap<>();

    /**
     * The ticket ids which are tied to this ticket.
     */
    @Lob
    @Column(name = "DESCENDANT_TICKETS", nullable = false, length = Integer.MAX_VALUE)
    private HashSet<String> descendantTickets = new HashSet<>();

    /**
     * Instantiates a new ticket granting ticket impl.
     */
    public TicketGrantingTicketImpl() {
    }

    /**
     * Constructs a new TicketGrantingTicket.
     * May throw an {@link IllegalArgumentException} if the Authentication object is null.
     *
     * @param id                         the id of the Ticket
     * @param proxiedBy                  Service that produced this proxy ticket.
     * @param parentTicketGrantingTicket the parent ticket
     * @param authentication             the Authentication request for this ticket
     * @param policy                     the expiration policy for this ticket.
     */
    @JsonCreator
    public TicketGrantingTicketImpl(@JsonProperty("id") final String id,
                                    @JsonProperty("proxiedBy") final Service proxiedBy,
                                    @JsonProperty("grantingTicket") final TicketGrantingTicket parentTicketGrantingTicket,
                                    @JsonProperty("authentication") final Authentication authentication,
                                    @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {

        super(id, policy);

        if (parentTicketGrantingTicket != null && proxiedBy == null) {
            throw new IllegalArgumentException("Must specify proxiedBy when providing parent TGT");
        }
        Assert.notNull(authentication, "authentication cannot be null");
        this.ticketGrantingTicket = parentTicketGrantingTicket;
        this.authentication = authentication;
        this.proxiedBy = proxiedBy;
    }

    /**
     * Constructs a new TicketGrantingTicket without a parent
     * TicketGrantingTicket.
     *
     * @param id             the id of the Ticket
     * @param authentication the Authentication request for this ticket
     * @param policy         the expiration policy for this ticket.
     */
    public TicketGrantingTicketImpl(final String id, final Authentication authentication, final ExpirationPolicy policy) {
        this(id, null, null, authentication, policy);
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    @Override
    public Authentication getAuthentication() {
        return this.authentication;
    }

    /**
     * {@inheritDoc}
     * <p>The state of the ticket is affected by this operation and the
     * ticket will be considered used. The state update subsequently may
     * impact the ticket expiration policy in that, depending on the policy
     * configuration, the ticket may be considered expired.
     */
    @Override
    public synchronized ServiceTicket grantServiceTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy,
                                                         final boolean credentialProvided, final boolean onlyTrackMostRecentSession) {

        final ServiceTicket serviceTicket = new ServiceTicketImpl(id, this,
                service, credentialProvided,
                expirationPolicy);

        trackServiceSession(serviceTicket.getId(), service, onlyTrackMostRecentSession);
        return serviceTicket;
    }

    /**
     * Update service and track session.
     *
     * @param id                         the id
     * @param service                    the service
     * @param onlyTrackMostRecentSession the only track most recent session
     */
    protected void trackServiceSession(final String id, final Service service, final boolean onlyTrackMostRecentSession) {
        update();

        service.setPrincipal(getRoot().getAuthentication().getPrincipal().getId());
        if (onlyTrackMostRecentSession) {
            final String path = normalizePath(service);
            final Collection<Service> existingServices = this.services.values();
            // loop on existing services
            existingServices.stream()
                    .filter(existingService -> path.equals(normalizePath(existingService)))
                    .findFirst().ifPresent(existingServices::remove);
        }
        this.services.put(id, service);
    }

    /**
     * Normalize the path of a service by removing the query string and everything after a semi-colon.
     *
     * @param service the service to normalize
     * @return the normalized path
     */
    private static String normalizePath(final Service service) {
        String path = service.getId();
        path = StringUtils.substringBefore(path, "?");
        path = StringUtils.substringBefore(path, ";");
        path = StringUtils.substringBefore(path, "#");
        return path;
    }

    /**
     * Gets an new map with the service ticket and services accessed by this ticket-granting ticket.
     *
     * @return a map of service ticket and services accessed by this ticket-granting ticket.
     */
    @Override
    public synchronized Map<String, Service> getServices() {
        return new HashMap<>(this.services);
    }
    
    @Override
    public Map<String, Service> getProxyGrantingTickets() {
        return this.proxyGrantingTickets;
    }

    /**
     * Remove all services of the TGT (at logout).
     */
    @Override
    public void removeAllServices() {
        this.services.clear();
    }

    /**
     * Return if the TGT has no parent.
     *
     * @return if the TGT has no parent.
     */
    @Override
    public boolean isRoot() {
        return this.getGrantingTicket() == null;
    }

    @Override
    public void markTicketExpired() {
        this.expired = Boolean.TRUE;
    }

    @JsonIgnore
    @Override
    public TicketGrantingTicket getRoot() {
        final TicketGrantingTicket parent = getGrantingTicket();
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    /**
     * Return if the TGT is expired.
     *
     * @return if the TGT is expired.
     */
    @Override
    @JsonIgnore
    public boolean isExpiredInternal() {
        return this.expired;
    }

    @JsonIgnore
    @Override
    public List<Authentication> getChainedAuthentications() {
        final List<Authentication> list = new ArrayList<>();

        list.add(getAuthentication());

        if (getGrantingTicket() == null) {
            return new ArrayList<>(list);
        }

        list.addAll(getGrantingTicket().getChainedAuthentications());
        return new ArrayList<>(list);
    }

    @Override
    public Service getProxiedBy() {
        return this.proxiedBy;
    }

    @Override
    public String getPrefix() {
        return TicketGrantingTicket.PREFIX;
    }

    @Override
    public Collection getDescendantTickets() {
        return descendantTickets;
    }
}
