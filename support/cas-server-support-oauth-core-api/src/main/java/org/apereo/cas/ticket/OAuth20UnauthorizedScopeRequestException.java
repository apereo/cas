package org.apereo.cas.ticket;

import module java.base;

/**
 * This is {@link OAuth20UnauthorizedScopeRequestException}.
 * The requested scope is invalid, unknown, malformed, or
 * exceeds the scope granted by the resource owner.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20UnauthorizedScopeRequestException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = -1123066598613188666L;

    public OAuth20UnauthorizedScopeRequestException(final String ticketId) {
        super(ticketId);
    }
}

