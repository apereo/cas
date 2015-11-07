package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.validation.Assertion;

/**
 * Concrete subclass of {@code AbstractCasEvent} representing validation of a
 * service ticket by a CAS server.
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasServiceTicketValidatedEvent extends AbstractCasEvent {

    private final Assertion assertion;

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
        return assertion;
    }

    public ServiceTicket getServiceTicket() {
        return serviceTicket;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("assertion", assertion)
                .append("serviceTicket", serviceTicket)
                .toString();
    }
}
