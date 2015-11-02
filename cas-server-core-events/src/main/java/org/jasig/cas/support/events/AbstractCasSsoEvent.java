package org.jasig.cas.support.events;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.Authentication;
import org.springframework.context.ApplicationEvent;

/**
 * Base Spring {@code ApplicationEvent} representing a abstract single sign on action executed within running CAS server.
 * <p/>
 * This event encapsulates {@link Authentication} that is associated with an SSO action executed in a CAS server and an SSO session
 * token in the form of ticket granting ticket id.
 * <p/>
 * More concrete events are expected to subclass this abstract type.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public abstract class AbstractCasSsoEvent extends ApplicationEvent {

    private final Authentication authentication;

    private final String ticketGrantingTicketId;

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source                 the source
     * @param authentication         the authentication
     * @param ticketGrantingTicketId the ticket granting ticket id
     */
    public AbstractCasSsoEvent(final Object source, final Authentication authentication,
                               final String ticketGrantingTicketId) {
        super(source);
        this.authentication = authentication;
        this.ticketGrantingTicketId = ticketGrantingTicketId;
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }

    public String getTicketGrantingTicketId() {
        return this.ticketGrantingTicketId;
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        return builder
                .append("authentication", authentication)
                .append("ticketGrantingTicketId", ticketGrantingTicketId)
                .toString();
    }
}
