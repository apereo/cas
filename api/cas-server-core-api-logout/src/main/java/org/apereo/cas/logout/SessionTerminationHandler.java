package org.apereo.cas.logout;

import module java.base;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SessionTerminationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface SessionTerminationHandler {
    /**
     * Before single logout.
     *
     * @param ticketGrantingTicketId the ticket granting ticket id
     * @param context                the context
     */
    default void beforeSingleLogout(final String ticketGrantingTicketId, final RequestContext context) {
    }

    /**
     * Before session termination list.
     *
     * @param requestContext the request context
     * @return the list
     */
    default List<? extends Serializable> beforeSessionTermination(final RequestContext requestContext) {
        return List.of();
    }

    /**
     * After session termination.
     *
     * @param terminationResults the termination results
     * @param requestContext     the request context
     */
    default void afterSessionTermination(final List<? extends Serializable> terminationResults, final RequestContext requestContext) {
    }
}
