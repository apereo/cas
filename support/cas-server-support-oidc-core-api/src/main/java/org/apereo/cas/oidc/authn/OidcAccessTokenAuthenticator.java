package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcAccessTokenAuthenticator}, which, when the access token carries an ID token, takes the subject and
 * the claims of the ID token into the profile. A token whose ID token cannot be decoded is refused.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcAccessTokenAuthenticator extends DefaultOAuth20AccessTokenAuthenticator {
    private final OAuth20TokenSigningAndEncryptionService idTokenSigningAndEncryptionService;

    private final ServicesManager servicesManager;

    @Override
    protected @Nullable OAuth20UserProfile buildUserProfile(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        val profile = super.buildUserProfile(request, accessToken);
        if (profile == null) {
            return null;
        }
        try {
            validateIdTokenIfAny(accessToken, profile);
            return profile;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return null;
        }
    }

    protected void validateIdTokenIfAny(final OAuth20AccessToken accessToken, final OAuth20UserProfile profile) throws Exception {
        if (StringUtils.isNotBlank(accessToken.getIdToken())) {
            val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
            val idTokenResult = idTokenSigningAndEncryptionService.decode(accessToken.getIdToken(), Optional.ofNullable(service));
            profile.setId(idTokenResult.getSubject());
            profile.addAttributes(idTokenResult.getClaimsMap());
        }
    }
}
