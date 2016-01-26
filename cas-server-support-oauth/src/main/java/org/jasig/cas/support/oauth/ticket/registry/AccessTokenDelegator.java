package org.jasig.cas.support.oauth.ticket.registry;

import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;

/**
 * This is a specific delegator for {@link AccessToken}.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public class AccessTokenDelegator<T extends AccessToken> extends OAuthCodeDelegator<T>
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
}
