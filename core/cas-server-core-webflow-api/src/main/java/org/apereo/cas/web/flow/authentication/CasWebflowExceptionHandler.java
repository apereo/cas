package org.apereo.cas.web.flow.authentication;

import org.springframework.core.Ordered;
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
     * Unknown event id, principal or action.
     */
    String UNKNOWN = "UNKNOWN";

    /**
     * Handle event.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return the event
     */
    Event handle(T exception, RequestContext requestContext);

    /**
     * Supports exception.
     *
     * @param exception      the exception
     * @param requestContext the request context
     * @return true/false
     */
    boolean supports(Exception exception, RequestContext requestContext);
}
