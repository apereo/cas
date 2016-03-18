package org.jasig.cas.support.oauth.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.ServiceTicketDelegator;

/**
 * This is a specific delegator for {@link RefreshToken}.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class RefreshTokenDelegator<T extends RefreshToken>  extends ServiceTicketDelegator<T>
        implements RefreshToken {

    private static final long serialVersionUID = -8894815797583715471L;

    /**
     * Instantiates a new refresh token delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param refreshToken the refresh token
     * @param callback the callback
     */
    public RefreshTokenDelegator(final AbstractTicketRegistry ticketRegistry,
                                 final T refreshToken, final boolean callback) {
        super(ticketRegistry, refreshToken, callback);
    }

    @Override
    public Authentication getAuthentication() {
        return getTicket().getAuthentication();
    }
}
