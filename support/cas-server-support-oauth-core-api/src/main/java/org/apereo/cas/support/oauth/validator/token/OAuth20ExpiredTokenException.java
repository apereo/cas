package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.ticket.InvalidTicketException;

/**
 * Thrown when a token request refers to a device code or authentication request that has expired,
 * reported to the client as {@code expired_token}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OAuth20ExpiredTokenException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = -2741918265307440153L;

    public OAuth20ExpiredTokenException(final String ticketId) {
        super(ticketId);
    }
}
