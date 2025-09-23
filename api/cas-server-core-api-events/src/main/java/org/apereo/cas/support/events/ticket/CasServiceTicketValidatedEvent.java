package org.apereo.cas.support.events.ticket;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.validation.Assertion;

import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

/**
 * Concrete subclass of {@link AbstractCasEvent} representing validation of a
 * service ticket by a CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString(callSuper = true)
@Getter
public class CasServiceTicketValidatedEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -1218257740549089556L;

    private final transient Assertion assertion;

    private final ServiceTicket serviceTicket;

    /**
     * Instantiates a new CAS service ticket validated event.
     *
     * @param source        the source
     * @param serviceTicket the service ticket
     * @param assertion     the assertion
     */
    public CasServiceTicketValidatedEvent(final Object source, final ServiceTicket serviceTicket,
                                          final Assertion assertion, final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.assertion = assertion;
        this.serviceTicket = serviceTicket;
    }
}
