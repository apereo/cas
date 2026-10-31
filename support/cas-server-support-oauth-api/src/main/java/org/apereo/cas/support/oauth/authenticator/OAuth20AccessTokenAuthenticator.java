package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Decides whether an access token, presented to an endpoint that CAS protects with access tokens (dynamic client
 * registration, client configuration, UMA), may be used for the request, and builds the profile of the request from
 * it. The token reaching the authenticator is known, unexpired and, when sender-constrained, presented with its proof.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OAuth20AccessTokenAuthenticator {

    /**
     * The scopes the access token must carry. A token without one of them is refused with {@code insufficient_scope}
     * (RFC 6750, section 3.1).
     *
     * @return the required scopes
     */
    default Set<String> getRequiredScopes() {
        return Set.of();
    }

    /**
     * The profile of the request.
     *
     * @param request     the request
     * @param accessToken the access token
     * @return the profile, or empty when the token may not be used for this request, which is refused with
     *     {@code invalid_token} (RFC 6750, section 3.1)
     */
    Optional<OAuth20UserProfile> authenticate(HttpServletRequest request, OAuth20AccessToken accessToken);
}
