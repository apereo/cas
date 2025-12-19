package org.apereo.cas.support.events;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import org.springframework.context.ApplicationEvent;

/**
 * Base Spring {@code ApplicationEvent} representing a abstract single sign on action executed within running CAS server.
 * This event encapsulates {@link Authentication} that is associated with an SSO action
 * executed in a CAS server and an SSO session
 * token in the form of ticket granting ticket id.
 * More concrete events are expected to subclass this abstract type.
 *
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@ToString
@Getter
public abstract class AbstractCasEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 8059647975948452375L;

    private final ClientInfo clientInfo;

    protected AbstractCasEvent(final Object source, final ClientInfo clientInfo) {
        super(source);
        this.clientInfo = clientInfo;
    }
}
