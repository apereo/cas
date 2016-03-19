package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;

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
        throw new UnsupportedOperationException("getExpirationPolicy() is not supported");
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
}
