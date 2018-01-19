package org.apereo.cas.support.events.ticket;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.validation.Assertion;
import lombok.ToString;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing validation of a
 * service ticket by a CAS server.
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@Slf4j
@ToString
public class CasServiceTicketValidatedEvent extends AbstractCasEvent {

    private static final long serialVersionUID = -1218257740549089556L;

    private final transient Assertion assertion;

    private final ServiceTicket serviceTicket;

    /**
     * Instantiates a new Cas service ticket validated event.
     *
     * @param source        the source
     * @param serviceTicket the service ticket
     * @param assertion     the assertion
     */
    public CasServiceTicketValidatedEvent(final Object source, final ServiceTicket serviceTicket, final Assertion assertion) {
        super(source);
        this.assertion = assertion;
        this.serviceTicket = serviceTicket;
    }

    public Assertion getAssertion() {
        return this.assertion;
    }

    public ServiceTicket getServiceTicket() {
        return this.serviceTicket;
    }
}
