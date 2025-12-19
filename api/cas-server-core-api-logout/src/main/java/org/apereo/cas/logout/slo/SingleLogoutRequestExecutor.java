package org.apereo.cas.logout.slo;

import module java.base;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Execute and initiate single logout operations
 * for a given session tied to a ticket.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface SingleLogoutRequestExecutor {

    /**
     * Bean name for default impl.
     */
    String BEAN_NAME = "defaultSingleLogoutRequestExecutor";

    /**
     * Execute.
     *
     * @param ticketId the ticket id
     * @param request  the request
     * @param response the response
     * @return the list
     */
    List<SingleLogoutRequestContext> execute(String ticketId,
                                             HttpServletRequest request,
                                             HttpServletResponse response);
}
