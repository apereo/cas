package org.jasig.cas.support.oauth.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.ServiceTicketDelegator;

/**
 * This is a specific delegator for {@link AccessToken}.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class AccessTokenDelegator<T extends AccessToken>  extends ServiceTicketDelegator<T>
        implements AccessToken {

    private static final long serialVersionUID = 3656947680544885480L;

    /**
     * Instantiates a new access token delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param accessToken the access token
     * @param callback the callback
     */
    public AccessTokenDelegator(final AbstractTicketRegistry ticketRegistry,
                         final T accessToken, final boolean callback) {
        super(ticketRegistry, accessToken, callback);
    }

    @Override
    public Authentication getAuthentication() {
        return getTicket().getAuthentication();
    }
}
