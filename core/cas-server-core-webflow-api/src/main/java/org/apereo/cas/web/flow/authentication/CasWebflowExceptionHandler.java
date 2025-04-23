package org.apereo.cas.web.flow.authentication;

import org.springframework.core.Ordered;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CasWebflowExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 6.1.0
 */
public interface CasWebflowExceptionHandler<T extends Exception> extends Ordered {
    /**
     * The event factory instance.
     */
    EventFactorySupport EVENT_FACTORY = new EventFactorySupport();

    /**
     * Handle event.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return the event
     * @throws Throwable the throwable
     */
    Event handle(T exception, RequestContext requestContext) throws Throwable;

    /**
     * Supports exception.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean supports(Exception exception, RequestContext requestContext) throws Throwable;
}
