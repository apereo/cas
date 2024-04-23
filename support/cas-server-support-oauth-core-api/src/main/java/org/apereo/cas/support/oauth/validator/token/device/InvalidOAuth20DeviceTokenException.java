package org.apereo.cas.support.oauth.validator.token.device;

import org.apereo.cas.ticket.InvalidTicketException;

import java.io.Serial;

/**
 * This is {@link InvalidOAuth20DeviceTokenException}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class InvalidOAuth20DeviceTokenException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = 1322463431237888487L;

    public InvalidOAuth20DeviceTokenException(final String ticketId) {
        super(ticketId);
    }
}
