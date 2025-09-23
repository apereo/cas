package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OidcUserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class OidcUserProfileViewRenderer extends OAuth20DefaultUserProfileViewRenderer {
    private final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService;


    public OidcUserProfileViewRenderer(final OAuthProperties oauthProperties,
                                       final ServicesManager servicesManager,
                                       final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService,
                                       final AttributeDefinitionStore attributeDefinitionStore) {
        super(servicesManager, oauthProperties, attributeDefinitionStore);
        this.signingAndEncryptionService = signingAndEncryptionService;
    }

    @Override
    protected ResponseEntity renderProfileForModel(final Map<String, Object> userProfile,
                                                   final OAuth20AccessToken accessToken,
                                                   final HttpServletResponse response) {
        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
        if (service instanceof final OidcRegisteredService oidcRegisteredService) {
            return FunctionUtils.doAndHandle(() -> {
                if (signingAndEncryptionService.shouldSignToken(oidcRegisteredService)
                    || signingAndEncryptionService.shouldEncryptToken(oidcRegisteredService)) {
                    return signAndEncryptUserProfileClaims(userProfile, response, oidcRegisteredService);
                }
                return buildPlainUserProfileClaims(userProfile, response, oidcRegisteredService);
            }, e -> ResponseEntity.badRequest().body("Unable to produce user profile claims")).get();
        }

        return super.renderProfileForModel(userProfile, accessToken, response);
    }

    protected ResponseEntity<String> buildPlainUserProfileClaims(final Map<String, Object> userProfile,
                                                                 final HttpServletResponse response,
                                                                 final OidcRegisteredService registeredService) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        val claims = convertUserProfileIntoClaims(userProfile);
        return buildResponseEntity(claims.toJson(), response, registeredService);
    }
    

    protected ResponseEntity<String> signAndEncryptUserProfileClaims(final Map<String, Object> userProfile,
                                                                     final HttpServletResponse response,
                                                                     final OidcRegisteredService registeredService) throws Throwable {
        val claims = convertUserProfileIntoClaims(userProfile);
        claims.setAudience(registeredService.getClientId());
        claims.setIssuedAt(NumericDate.now());
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(signingAndEncryptionService.resolveIssuer(Optional.of(registeredService)));

        LOGGER.debug("Collected user profile claims, before cipher operations, are [{}]", claims);
        val result = signingAndEncryptionService.encode(registeredService, claims);
        LOGGER.debug("Finalized user profile is [{}]", result);

        response.setContentType(OidcConstants.CONTENT_TYPE_JWT);
        return buildResponseEntity(result, response, registeredService);
    }

    private static ResponseEntity<String> buildResponseEntity(final String result, final HttpServletResponse response,
                                                              final OidcRegisteredService registeredService) {
        val context = Map.<String, Object>of(
            "Client ID", registeredService.getClientId(),
            "Service", registeredService.getName(),
            "Content Type", response.getContentType());
        LoggingUtils.protocolMessage("OpenID Connect User Profile Response", context, result);
        
        val headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, CollectionUtils.wrapList(response.getContentType()));
        return ResponseEntity.ok().headers(headers).body(result);
    }
}
