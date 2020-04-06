package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.OAuth20Token;

import java.util.HashSet;
import java.util.Set;

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
     * Gets descendant OAuth access tokens.
     * The revocation of a refresh token may cause the revocation of related
     * tokens and the underlying authorization grant. If a refresh token is
     * revoked, the authorization server SHOULD
     * also invalidate all access tokens based on the same authorization
     * grant. Here, we track the access tokens.
     *
     * @return the access tokens
     * @since 6.2
     */
    default Set<String> getAccessTokens() {
        return new HashSet<>(0);
    }
}
