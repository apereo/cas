package org.apereo.cas.support.events.ticket;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import java.io.Serial;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing granting of a
 * proxy ticket by a CAS server.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasProxyTicketGrantedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = 128616377249711105L;

    private final ProxyGrantingTicket proxyGrantingTicket;

    private final Ticket proxyTicket;

    public CasProxyTicketGrantedEvent(final Object source, final ProxyGrantingTicket proxyGrantingTicket,
                                      final Ticket proxyTicket, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.proxyGrantingTicket = proxyGrantingTicket;
        this.proxyTicket = proxyTicket;
    }
}
