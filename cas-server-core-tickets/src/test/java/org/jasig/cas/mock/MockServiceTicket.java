package org.jasig.cas.mock;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.util.Date;

/**
 * Mock service ticket.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockServiceTicket implements ServiceTicket {

    private static final long serialVersionUID = 8203377063087967768L;

    private final String id;

    private final Date created;

    private final Service service;

    private final TicketGrantingTicket parent;

    public MockServiceTicket(final String id, final Service service, final TicketGrantingTicket parent) {
        this.service = service;
        this.id = id;
        this.parent = parent;
        created = new Date();
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
    public long getCreationTime() {
        return created.getTime();
    }

    @Override
    public int getCountOfUses() {
        return 0;
    }
}
