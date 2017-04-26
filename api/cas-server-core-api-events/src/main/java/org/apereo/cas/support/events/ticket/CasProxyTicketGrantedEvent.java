package org.apereo.cas.support.events.ticket;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing granting of a
 * proxy ticket by a CAS server.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class CasProxyTicketGrantedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = 128616377249711105L;

    private final ProxyGrantingTicket proxyGrantingTicket;
    private final ProxyTicket proxyTicket;

    /**
     * Instantiates a new Cas proxy ticket granted event.
     *
     * @param source               the source
     * @param proxyGrantingTicket the ticket granting ticket
     * @param proxyTicket        the service ticket
     */
    public CasProxyTicketGrantedEvent(final Object source, final ProxyGrantingTicket proxyGrantingTicket,
                                      final ProxyTicket proxyTicket) {
        super(source);
        this.proxyGrantingTicket = proxyGrantingTicket;
        this.proxyTicket = proxyTicket;
    }

    public ProxyGrantingTicket getProxyGrantingTicket() {
        return this.proxyGrantingTicket;
    }

    public ProxyTicket getProxyTicket() {
        return this.proxyTicket;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("proxyGrantingTicket", this.proxyGrantingTicket)
                .append("proxyTicket", this.proxyTicket)
                .toString();
    }
}
