package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Domain object representing a Service Ticket. A service ticket grants specific
 * access to a particular service. It will only work for a particular service.
 * Generally, it is a one time use Ticket, but the specific expiration policy
 * can be anything.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Slf4j
@Setter
@NoArgsConstructor
@Getter
@Accessors(chain = true)
@SuppressWarnings("NullAway.Init")
public class ServiceTicketImpl extends AbstractTicket
    implements ServiceTicket, RenewableServiceTicket, ProxyGrantingTicketIssuerTicket {

    @Serial
    private static final long serialVersionUID = -4223319704861765405L;

    @JsonProperty("ticketGrantingTicket")
    @Nullable
    private TicketGrantingTicket ticketGrantingTicket;

    @JsonProperty("authentication")
    private Authentication authentication;

    private Service service;

    @JsonSetter(nulls = Nulls.SKIP)
    private boolean fromNewLogin;

    @JsonSetter(nulls = Nulls.SKIP)
    private Boolean grantedTicketAlready = Boolean.FALSE;

    @JsonCreator
    public ServiceTicketImpl(
        @JsonProperty("id") final @NonNull String id,
        @Nullable @JsonProperty("ticketGrantingTicket") final TicketGrantingTicket ticket,
        @JsonProperty("service") final @NonNull Service service,
        @JsonProperty("credentialProvided")
        @JsonSetter(nulls = Nulls.SKIP)
        final boolean credentialProvided,
        @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {
        super(id, policy);
        this.ticketGrantingTicket = ticket;
        this.service = Objects.requireNonNull(service);
        this.fromNewLogin = credentialProvided || (ticket != null && ticket.getCountOfUses() == 0);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(
        final @NonNull String id, final @NonNull Authentication authentication,
        final ExpirationPolicy expirationPolicy,
        final TicketTrackingPolicy proxyGrantingTicketTrackingPolicy) throws AbstractTicketException {
        if (this.grantedTicketAlready) {
            LOGGER.warn("Service ticket [{}] issued for service [{}] has already allotted a proxy-granting ticket", getId(), service.getId());
            throw new InvalidProxyGrantingTicketForServiceTicketException(service);
        }
        this.grantedTicketAlready = Boolean.TRUE;
        val proxyGrantingTicket = new ProxyGrantingTicketImpl(id, service, ticketGrantingTicket, authentication, expirationPolicy);
        proxyGrantingTicket.setTenantId(service.getTenant());
        proxyGrantingTicketTrackingPolicy.trackTicket(ticketGrantingTicket, proxyGrantingTicket, service);
        return proxyGrantingTicket;
    }

    @Override
    @JsonIgnore
    public Authentication getAuthentication() {
        return Objects.requireNonNullElseGet(authentication, () -> ticketGrantingTicket != null ? ticketGrantingTicket.getAuthentication() : null);
    }

    @Override
    public String getPrefix() {
        return ServiceTicket.PREFIX;
    }
}
