package org.apereo.cas.oidc.profile;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.ticket.OAuthTokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;

import java.util.Map;
import java.util.UUID;

/**
 * This is {@link OidcUserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcUserProfileViewRenderer extends OAuth20DefaultUserProfileViewRenderer {
    private final ServicesManager servicesManager;
    private final OAuthTokenSigningAndEncryptionService signingAndEncryptionService;

    public OidcUserProfileViewRenderer(final OAuthProperties oauthProperties,
                                       final ServicesManager servicesManager,
                                       final OAuthTokenSigningAndEncryptionService signingAndEncryptionService) {
        super(oauthProperties);
        this.servicesManager = servicesManager;
        this.signingAndEncryptionService = signingAndEncryptionService;
    }

    @Override
    protected String renderProfileForModel(final Map<String, Object> userProfile, final AccessToken accessToken) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, accessToken.getClientId());
        if (registeredService instanceof OidcRegisteredService) {
            val claims = new JwtClaims();
            userProfile.forEach(claims::setClaim);
            claims.setAudience(registeredService.getClientId());
            claims.setIssuedAt(NumericDate.now());
            claims.setIssuer(this.signingAndEncryptionService.getIssuer());
            claims.setJwtId(UUID.randomUUID().toString());

            LOGGER.debug("Collected user profile claims are [{}]", claims);
            val result = this.signingAndEncryptionService.encode(registeredService, claims);
            LOGGER.debug("Finalized user profile is [{}]", result);
        }
        return super.renderProfileForModel(userProfile, accessToken);
    }
}
