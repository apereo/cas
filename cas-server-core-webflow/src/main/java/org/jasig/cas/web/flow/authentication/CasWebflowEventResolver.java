package org.jasig.cas.web.flow.authentication;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link CasWebflowEventResolver}
 * that decides the next event in the authentication web flow.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public interface CasWebflowEventResolver {

    /**
     * Resolve event.
     *
     * @param context the context
     * @return the event
     */
    Set<Event> resolve(RequestContext context);


    /**
     * Resolve single event.
     *
     * @param context the context
     * @return the event
     */
    Event resolveSingle(RequestContext context);
}
