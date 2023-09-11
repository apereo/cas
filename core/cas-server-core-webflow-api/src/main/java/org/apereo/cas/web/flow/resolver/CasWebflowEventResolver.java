package org.apereo.cas.web.flow.resolver;

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
public interface CasWebflowEventResolver {

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
     * Define the name of this even resolver.
     *
     * @return name of the resolver.
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Resolve internal event.
     *
     * @param context the context
     * @return the event
     * @throws Throwable the throwable
     */
    Set<Event> resolveInternal(RequestContext context) throws Throwable;
}
