package org.apereo.cas.mock;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Mock service ticket.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"id"})
public class MockServiceTicket implements ServiceTicket, TicketState {

    private static final long serialVersionUID = 8203377063087967768L;

    private final String id;

    private final ZonedDateTime created;

    private final Service service;
    private final TicketGrantingTicket parent;
    private ExpirationPolicy expiration = new NeverExpiresExpirationPolicy();
    private boolean expired;

    public MockServiceTicket(final String id, final Service service, final TicketGrantingTicket parent) {
        this.service = service;
        this.id = id;
        this.parent = parent;
        created = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public boolean isFromNewLogin() {
        return false;
    }

    @Override
    public boolean isValidFor(final Service service) {
        return this.service.equals(service);
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id, final Authentication authentication, final ExpirationPolicy expirationPolicy) {
        return null;
    }

    @Override
    public ExpirationPolicy getExpirationPolicy() {
        return this.expiration;
    }

    @Override
    public String getPrefix() {
        return ServiceTicket.PREFIX;
    }

    @Override
    public boolean isExpired() {
        return this.expired || this.expiration.isExpired(this);
    }

    @Override
    public TicketGrantingTicket getTicketGrantingTicket() {
        return parent;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return created;
    }

    @Override
    public Authentication getAuthentication() {
        return this.parent.getAuthentication();
    }

    @Override
    public void update() {
    }

    @Override
    public int getCountOfUses() {
        return 0;
    }

    @Override
    public ZonedDateTime getLastTimeUsed() {
        return ZonedDateTime.now();
    }

    @Override
    public ZonedDateTime getPreviousTimeUsed() {
        return ZonedDateTime.now();
    }

    @Override
    public int compareTo(final Ticket o) {
        return this.id.compareTo(o.getId());
    }

    @Override
    public void markTicketExpired() {
        this.expired = true;
    }
}
