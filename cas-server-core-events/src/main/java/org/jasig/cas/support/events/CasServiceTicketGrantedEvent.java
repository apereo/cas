package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

/**
 * Concrete subclass of {@code AbstractCasServiceAccessEvent} representing granting of a
 * service ticket by a CAS server.
 * This subclass adds {@link Authentication} that is associated with this event to the encapsulated data.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public final class CasServiceTicketGrantedEvent extends AbstractCasServiceAccessEvent {

    private static final long serialVersionUID = 128616377249711105L;
    private final Authentication authentication;

    /**
     * Instantiates a new Cas service ticket granted event.
     *
     * @param source          the source
     * @param serviceTicketId the service ticket id
     * @param service         the service
     * @param authentication  the authentication
     */
    public CasServiceTicketGrantedEvent(final Object source, final String serviceTicketId, final Service service,
                                        final Authentication authentication) {
        super(source, serviceTicketId, service);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        return builder.appendSuper(super.toString())
                .append("authentication", authentication)
                .toString();
    }
}
