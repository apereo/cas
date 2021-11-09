package org.apereo.cas.logout.slo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
