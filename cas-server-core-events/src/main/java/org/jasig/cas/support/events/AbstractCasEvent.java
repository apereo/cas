package org.jasig.cas.support.events;

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
public abstract class AbstractCasEvent extends ApplicationEvent {

    /**
     * Instantiates a new Abstract cas sso event.
     *
     * @param source                 the source
     */
    public AbstractCasEvent(final Object source) {
        super(source);
    }

}
