package org.apereo.cas.ticket.proxy.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.proxy.ProxyHandler;


/**
 * Dummy ProxyHandler that does nothing. Useful for CAS 1.0 compliance as CAS
 * 1.0 has no proxying capabilities.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class Cas10ProxyHandler implements ProxyHandler {

    @Override
    public String handle(final Credential credential, final Ticket proxyGrantingTicket) {
        return null;
    }

    @Override
    public boolean canHandle(final Credential credential) {
        return false;
    }
}
