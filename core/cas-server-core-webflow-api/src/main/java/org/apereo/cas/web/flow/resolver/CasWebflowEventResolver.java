package org.apereo.cas.web.flow.resolver;

import org.apereo.cas.util.NamedObject;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link CasWebflowEventResolver}
 * that decides the next event in the authentication web flow.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowEventResolver extends NamedObject {

    /**
     * The bean name for the webflow event resolver that handles service ticket requests.
     */
    String BEAN_NAME_SERVICE_TICKET_EVENT_RESOLVER = "serviceTicketRequestWebflowEventResolver";

    /**
     * Resolve event.
     *
     * @param context the context
     * @return the event
     * @throws Throwable the throwable
     */
    Set<Event> resolve(RequestContext context) throws Throwable;

    /**
     * Resolve single event.
     *
     * @param context the context
     * @return the event
     * @throws Throwable the throwable
     */
    Event resolveSingle(RequestContext context) throws Throwable;

    /**
     * Resolve internal event.
     *
     * @param context the context
     * @return the event
     * @throws Throwable the throwable
     */
    Set<Event> resolveInternal(RequestContext context) throws Throwable;
}
