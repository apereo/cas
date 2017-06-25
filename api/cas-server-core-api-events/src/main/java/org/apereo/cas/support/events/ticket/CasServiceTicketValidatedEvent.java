package org.apereo.cas.support.events.ticket;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.validation.Assertion;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing validation of a
 * service ticket by a CAS server.
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
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
    public CasServiceTicketValidatedEvent(final Object source,
                                          final ServiceTicket serviceTicket,
                                          final Assertion assertion) {
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


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("assertion", this.assertion)
                .append("serviceTicket", this.serviceTicket)
                .toString();
    }
}
