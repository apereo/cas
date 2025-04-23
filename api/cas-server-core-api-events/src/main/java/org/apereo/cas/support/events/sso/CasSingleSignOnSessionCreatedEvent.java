package org.apereo.cas.support.events.sso;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.Ticket;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import java.io.Serial;

/**
 * This is {@link CasSingleSignOnSessionCreatedEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@ToString(callSuper = true)
public class CasSingleSignOnSessionCreatedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -2862937393590213844L;

    @Getter
    private final Ticket ticketGrantingTicket;

    public CasSingleSignOnSessionCreatedEvent(final Object source, final Ticket ticketGrantingTicket,
                                              final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.ticketGrantingTicket = ticketGrantingTicket;
    }
}
