package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serial;
import java.util.Objects;

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
public class ServiceTicketImpl extends AbstractTicket
    implements ServiceTicket, RenewableServiceTicket, ProxyGrantingTicketIssuerTicket {

    @Serial
    private static final long serialVersionUID = -4223319704861765405L;

    @JsonProperty("ticketGrantingTicket")
    private TicketGrantingTicket ticketGrantingTicket;

    @JsonProperty("authentication")
    private Authentication authentication;

    private Service service;

    private boolean fromNewLogin;

    private Boolean grantedTicketAlready = Boolean.FALSE;

    @JsonCreator
    public ServiceTicketImpl(
        @JsonProperty("id") final @NonNull String id,
        @JsonProperty("ticketGrantingTicket") final TicketGrantingTicket ticket,
        @JsonProperty("service") final @NonNull Service service,
        @JsonProperty("credentialProvided") final boolean credentialProvided,
        @JsonProperty("expirationPolicy") final ExpirationPolicy policy) {
        super(id, policy);
        this.ticketGrantingTicket = ticket;
        this.service = service;
        this.fromNewLogin = credentialProvided || (ticket != null && ticket.getCountOfUses() == 0);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(
        final @NonNull String id, final @NonNull Authentication authentication,
        final ExpirationPolicy expirationPolicy) throws AbstractTicketException {
        if (this.grantedTicketAlready) {
            LOGGER.warn("Service ticket [{}] issued for service [{}] has already allotted a proxy-granting ticket", getId(), service.getId());
            throw new InvalidProxyGrantingTicketForServiceTicketException(service);
        }
        this.grantedTicketAlready = Boolean.TRUE;
        val proxyGrantingTicket = new ProxyGrantingTicketImpl(id, service, ticketGrantingTicket, authentication, expirationPolicy);
        if (ticketGrantingTicket != null) {
            ticketGrantingTicket.getProxyGrantingTickets().put(proxyGrantingTicket.getId(), service);
        }
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
