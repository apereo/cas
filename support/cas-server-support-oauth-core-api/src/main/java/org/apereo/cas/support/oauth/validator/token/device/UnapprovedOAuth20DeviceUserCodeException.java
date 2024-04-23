package org.apereo.cas.support.oauth.validator.token.device;

import org.apereo.cas.ticket.InvalidTicketException;

import java.io.Serial;

/**
 * This is {@link UnapprovedOAuth20DeviceUserCodeException}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UnapprovedOAuth20DeviceUserCodeException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = -3323066598613188666L;

    public UnapprovedOAuth20DeviceUserCodeException(final String ticketId) {
        super(ticketId);
    }
}
