package org.apereo.cas.mock;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.support.NeverExpiresExpirationPolicy;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Mock service ticket.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockServiceTicket implements ServiceTicket {

    private static final long serialVersionUID = 8203377063087967768L;

    private String id;

    private ZonedDateTime created;

    private Service service;
    
    private ExpirationPolicy expiration = new NeverExpiresExpirationPolicy();
    
    private TicketGrantingTicket parent;

    public MockServiceTicket(final String id, final Service service, final TicketGrantingTicket parent) {
        this.service = service;
        this.id = id;
        this.parent = parent;
        created = ZonedDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public Service getService() {
        return service;
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
    public ProxyGrantingTicket grantProxyGrantingTicket(
            final String id,
            final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
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
    public String getId() {
        return id;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return parent;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        return created;
    }

    @Override
    public int getCountOfUses() {
        return 0;
    }


    @Override
    public int compareTo(final Ticket o) {
        return this.id.compareTo(o.getId());
    }

    @Override
    public boolean equals(final Object obj) {
        return compareTo((Ticket) obj) == 0;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public void setExpiration(final ExpirationPolicy expiration) {
        this.expiration = expiration;
    }
}
