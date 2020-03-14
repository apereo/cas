package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.OAuth20Token;

import java.util.Collection;
import java.util.HashSet;

/**
 * A refresh token is an OAuth token which can be used multiple times and has a very long lifetime.
 * It is used to create new access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20RefreshToken extends OAuth20Token {

    /**
     * The prefix for refresh tokens.
     */
    String PREFIX = "RT";

    /**
     * Client id for whom this access token was issued.
     *
     * @return client id.
     * @since 6.2
     */
    String getClientId();

    /**
     * Gets descendant OAuth access tokens.
     *
     * @return the descendant tickets
     * @since 6.2
     */
    default Collection<String> getDescendantTickets() {
        return new HashSet<>(0);
    }
}
