package org.apereo.cas.ticket;

/**
 * This is {@link UnauthorizedScopeRequestException}.
 * The requested scope is invalid, unknown, malformed, or
 * exceeds the scope granted by the resource owner.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class UnauthorizedScopeRequestException extends InvalidTicketException {
    private static final long serialVersionUID = -1123066598613188666L;

    public UnauthorizedScopeRequestException(final String ticketId) {
        super(ticketId);
    }
}

