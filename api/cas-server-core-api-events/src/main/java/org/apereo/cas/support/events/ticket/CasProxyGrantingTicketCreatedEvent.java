package org.apereo.cas.support.events.ticket;

import module java.base;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing single sign on session establishment
 * event e.g. user logged in
 * and {@link ProxyGrantingTicket} has been vended by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasProxyGrantingTicketCreatedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -1862937393590213844L;

    private final Ticket proxyGrantingTicket;

    public CasProxyGrantingTicketCreatedEvent(final Object source, final Ticket proxyGrantingTicket, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.proxyGrantingTicket = proxyGrantingTicket;
    }
}
