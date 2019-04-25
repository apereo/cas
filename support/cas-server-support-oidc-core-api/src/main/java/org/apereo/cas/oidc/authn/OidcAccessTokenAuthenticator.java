package org.apereo.cas.oidc.authn;

import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.ticket.OAuthTokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;

/**
 * This is {@link OidcAccessTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcAccessTokenAuthenticator extends OAuth20AccessTokenAuthenticator {
    private final OAuthTokenSigningAndEncryptionService idTokenSigningAndEncryptionService;

    public OidcAccessTokenAuthenticator(final TicketRegistry ticketRegistry, final OAuthTokenSigningAndEncryptionService signingAndEncryptionService) {
        super(ticketRegistry);
        this.idTokenSigningAndEncryptionService = signingAndEncryptionService;
    }

    @Override
    protected CommonProfile buildUserProfile(final TokenCredentials tokenCredentials, final WebContext webContext, final AccessToken accessToken) {
        try {
            val profile = super.buildUserProfile(tokenCredentials, webContext, accessToken);
            validateIdTokenIfAny(accessToken, profile);
            return profile;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Validate id token if any.
     *
     * @param accessToken the access token
     * @param profile     the profile
     * @throws MalformedClaimException the malformed claim exception
     */
    protected void validateIdTokenIfAny(final AccessToken accessToken, final CommonProfile profile) throws MalformedClaimException {
        if (StringUtils.isNotBlank(accessToken.getIdToken())) {
            val idTokenResult = idTokenSigningAndEncryptionService.validate(accessToken.getIdToken());
            profile.setId(idTokenResult.getSubject());
            profile.addAttributes(idTokenResult.getClaimsMap());
        }
    }
}
