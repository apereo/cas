package org.apereo.cas.support.oauth.validator.token.device;

import module java.base;
import org.apereo.cas.ticket.InvalidTicketException;

/**
 * This is {@link ThrottledOAuth20DeviceUserCodeApprovalException}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class ThrottledOAuth20DeviceUserCodeApprovalException extends InvalidTicketException {
    @Serial
    private static final long serialVersionUID = 1487144100377263229L;

    public ThrottledOAuth20DeviceUserCodeApprovalException(final String ticketId) {
        super(ticketId);
    }
}
