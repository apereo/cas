package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.springframework.stereotype.Component;

/**
 * Dummy ProxyHandler that does nothing. Useful for Cas 1.0 compliance as CAS
 * 1.0 has no proxying capabilities.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("proxy10Handler")
public final class Cas10ProxyHandler implements ProxyHandler {

    @Override
    public String handle(final Credential credential,
        final TicketGrantingTicket proxyGrantingTicketId) {
        return null;
    }

    @Override
    public boolean canHandle(final Credential credential) {
        return false;
    }
}
