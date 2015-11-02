package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.validation.Assertion;

/**
 * Concrete subclass of <code>AbstractCasServiceAccessEvent</code> representing validation of a
 * service ticket by a CAS server.
 * This subclass adds {@link Assertion} that is associated with this event to the encapsulated data.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasServiceTicketValidatedEvent extends AbstractCasServiceAccessEvent {

    private final Assertion assertion;

    /**
     * Instantiates a new Cas service ticket validated event.
     *
     * @param source          the source
     * @param serviceTicketId the service ticket id
     * @param service         the service
     * @param assertion       the assertion
     */
    public CasServiceTicketValidatedEvent(final Object source,
                                          final String serviceTicketId,
                                          final Service service,
                                          final Assertion assertion) {
        super(source, serviceTicketId, service);
        this.assertion = assertion;
    }

    public Assertion getAssertion() {
        return assertion;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        return builder.appendSuper(super.toString())
               .append("assertion", assertion)
               .toString();
    }
}
