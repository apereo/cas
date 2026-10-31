package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcClientConfigurationAccessTokenAuthenticator}, which accepts a registration access token
 * (RFC 7592, section 2) only for the client it was issued to, named by the {@code client_id} parameter of the request,
 * and only with the {@code client_configuration} scope.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcClientConfigurationAccessTokenAuthenticator extends DefaultOAuth20AccessTokenAuthenticator {
    public OidcClientConfigurationAccessTokenAuthenticator() {
        setRequiredScopes(Set.of(OidcConstants.CLIENT_CONFIGURATION_SCOPE));
    }

    @Override
    protected @Nullable OAuth20UserProfile buildUserProfile(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        val requestedClientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        if (!Objects.equals(accessToken.getClientId(), requestedClientId)) {
            LOGGER.warn("Registration access token of client [{}] is not accepted for client [{}]", accessToken.getClientId(), requestedClientId);
            return null;
        }
        return super.buildUserProfile(request, accessToken);
    }
}
