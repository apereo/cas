package org.apereo.cas.oidc.profile;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
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
    private final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService;

    public OidcUserProfileViewRenderer(final OAuthProperties oauthProperties,
                                       final ServicesManager servicesManager,
                                       final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService) {
        super(oauthProperties);
        this.servicesManager = servicesManager;
        this.signingAndEncryptionService = signingAndEncryptionService;
    }

    @Override
    protected ResponseEntity renderProfileForModel(final Map<String, Object> userProfile, final OAuth20AccessToken accessToken,
                                                   final HttpServletResponse response) {
        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, accessToken.getClientId());
        if (!(service instanceof OidcRegisteredService)) {
            return super.renderProfileForModel(userProfile, accessToken, response);
        }

        val registeredService = (OidcRegisteredService) service;
        if (signingAndEncryptionService.shouldSignToken(registeredService) || signingAndEncryptionService.shouldEncryptToken(registeredService)) {
            val claims = new JwtClaims();
            userProfile.forEach(claims::setClaim);
            claims.setAudience(registeredService.getClientId());
            claims.setIssuedAt(NumericDate.now());
            claims.setIssuer(this.signingAndEncryptionService.getIssuer());
            claims.setJwtId(UUID.randomUUID().toString());

            LOGGER.debug("Collected user profile claims, before cipher operations, are [{}]", claims);
            val result = this.signingAndEncryptionService.encode(registeredService, claims);
            LOGGER.debug("Finalized user profile is [{}]", result);

            response.setContentType(OidcConstants.CONTENT_TYPE_JWT);
            val headers = new HttpHeaders();
            headers.put("Content-Type", CollectionUtils.wrapList(OidcConstants.CONTENT_TYPE_JWT));
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        val result = OAuth20Utils.toJson(userProfile);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
