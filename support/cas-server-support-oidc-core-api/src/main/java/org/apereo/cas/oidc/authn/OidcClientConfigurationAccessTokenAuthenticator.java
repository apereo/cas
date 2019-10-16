package org.apereo.cas.oidc.authn;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;

/**
 * This is {@link OidcClientConfigurationAccessTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcClientConfigurationAccessTokenAuthenticator extends OAuth20AccessTokenAuthenticator {
    public OidcClientConfigurationAccessTokenAuthenticator(final TicketRegistry ticketRegistry,
                                                           final JwtBuilder accessTokenJwtBuilder) {
        super(ticketRegistry, accessTokenJwtBuilder);
    }

    @Override
    protected CommonProfile buildUserProfile(final TokenCredentials tokenCredentials, final WebContext webContext, final OAuth20AccessToken accessToken) {
        try {
            val profile = super.buildUserProfile(tokenCredentials, webContext, accessToken);
            LOGGER.trace("Examining access token [{}] for required scope [{}]", accessToken, OidcConstants.CLIENT_REGISTRATION_SCOPE);
            if (accessToken.getScopes().contains(OidcConstants.CLIENT_REGISTRATION_SCOPE)) {
                return profile;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
