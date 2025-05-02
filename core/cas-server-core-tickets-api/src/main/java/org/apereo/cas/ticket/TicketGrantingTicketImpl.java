package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@NoArgsConstructor
public class TicketGrantingTicketImpl extends AbstractTicket implements TicketGrantingTicket {

    @Serial
    private static final long serialVersionUID = -8608149809180911599L;

    /**
     * The authenticated object for which this ticket was generated for.
     */
    private Authentication authentication;

    /**
     * Service that produced a proxy-granting ticket.
     */
    private Service proxiedBy;

    /**
     * The services associated to this ticket.
     */
    private Map<String, Service> services = new ConcurrentHashMap<>(0);

    /**
     * The {@link TicketGrantingTicket} this is associated with.
     */
    private TicketGrantingTicket ticketGrantingTicket;

    /**
     * The PGTs associated to this ticket.
     */
    private Map<String, Service> proxyGrantingTickets = new HashMap<>();

    /**
     * The ticket ids which are tied to this ticket.
     */
    private Set<String> descendantTickets = new HashSet<>(0);

    @JsonCreator
    public TicketGrantingTicketImpl(
        @JsonProperty("id") final String id,
        @JsonProperty("proxiedBy") final Service proxiedBy,
        @JsonProperty("ticketGrantingTicket") final TicketGrantingTicket ticketGrantingTicket,
        @JsonProperty("authentication") final @NonNull Authentication authentication,
        @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {
        super(id, policy);
        if (ticketGrantingTicket != null && proxiedBy == null) {
            throw new IllegalArgumentException("Must specify proxiedBy when providing parent ticket-granting ticket");
        }
        this.ticketGrantingTicket = ticketGrantingTicket;
        this.authentication = authentication;
        this.proxiedBy = proxiedBy;
    }

    public TicketGrantingTicketImpl(final String id, final Authentication authentication, final ExpirationPolicy policy) {
        this(id, null, null, authentication, policy);
    }

    @Override
    public ServiceTicket grantServiceTicket(
        final String id, final Service service,
        final ExpirationPolicy expirationPolicy,
        final boolean credentialProvided,
        final TicketTrackingPolicy trackingPolicy) {
        val serviceTicket = new ServiceTicketImpl(id, this, service, credentialProvided, expirationPolicy);
        serviceTicket.setTenantId(service.getTenant());
        trackingPolicy.trackTicket(this, serviceTicket);
        return serviceTicket;
    }

    @Override
    public void removeAllServices() {
        services.clear();
    }

    @Override
    public boolean isRoot() {
        return this.getTicketGrantingTicket() == null;
    }

    @JsonIgnore
    @Override
    @CanIgnoreReturnValue
    public TicketGrantingTicket getRoot() {
        val parent = this.getTicketGrantingTicket();
        if (parent == null) {
            return this;
        }
        return parent.getRoot();
    }

    @JsonIgnore
    @Override
    public List<Authentication> getChainedAuthentications() {
        val list = new ArrayList<Authentication>(2);
        list.add(getAuthentication());
        if (getTicketGrantingTicket() == null) {
            return list;
        }
        list.addAll(getTicketGrantingTicket().getChainedAuthentications());
        return list;
    }

    @Override
    public String getPrefix() {
        return TicketGrantingTicket.PREFIX;
    }

}
