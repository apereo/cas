package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.context.ApplicationEvent;

/**
 * Base Spring {@code ApplicationEvent} representing a service access action executed within running CAS server.
 * <p/>
 * This event encapsulates {@link Service} and <i>serviceTicketId</i> that are associated with an abstract
 * service access action executed in a CAS server.
 * <p/>
 * More concrete events are expected to subclass this abstract type.
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @since 1.1
 */
public class AbstractCasServiceAccessEvent extends ApplicationEvent {

    private static final long serialVersionUID = -8747961770235102170L;
    private final String serviceTicketId;

    private final Service service;

    /**
     * Instantiates a new Abstract cas service access event.
     *
     * @param source          the source
     * @param serviceTicketId the service ticket id
     * @param service         the service
     */
    public AbstractCasServiceAccessEvent(final Object source, final String serviceTicketId, final Service service) {
        super(source);
        this.serviceTicketId = serviceTicketId;
        this.service = service;
    }

    public String getServiceTicketId() {
        return serviceTicketId;
    }

    public Service getService() {
        return service;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        return builder.appendSuper(super.toString())
                .append("serviceTicketId", serviceTicketId)
                .append("service", service)
                .toString();
    }
}
