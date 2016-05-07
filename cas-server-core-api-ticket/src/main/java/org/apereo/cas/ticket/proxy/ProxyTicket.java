package org.apereo.cas.ticket.proxy;

import org.apereo.cas.ticket.ServiceTicket;

/**
 * The {@link ProxyTicket} represents a CAS proxy ticket. A proxy ticket is an opaque string that a
 * service uses as a credential to obtain access to a back-end service on behalf of a client.
 * Proxy tickets are obtained from CAS upon a service’s
 * presentation of a valid {@link ProxyGrantingTicket}
 * and a service identifier for the back-end service to which it is connecting.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface ProxyTicket extends ServiceTicket {
    /** Proxy ticket prefix applied to unique ids. */
    String PROXY_TICKET_PREFIX = "PT";
}
