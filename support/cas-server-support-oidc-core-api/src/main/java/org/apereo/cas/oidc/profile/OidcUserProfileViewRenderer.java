package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.claims.OidcAttributeDefinition;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
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

    private final AttributeDefinitionStore attributeDefinitionStore;

    public OidcUserProfileViewRenderer(final OAuthProperties oauthProperties,
                                       final ServicesManager servicesManager,
                                       final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService,
                                       final AttributeDefinitionStore attributeDefinitionStore) {
        super(servicesManager, oauthProperties);
        this.signingAndEncryptionService = signingAndEncryptionService;
        this.attributeDefinitionStore = attributeDefinitionStore;
    }

    @Override
    protected ResponseEntity renderProfileForModel(final Map<String, Object> userProfile,
                                                   final OAuth20AccessToken accessToken,
                                                   final HttpServletResponse response) {
        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
        if (!(service instanceof OidcRegisteredService)) {
            return super.renderProfileForModel(userProfile, accessToken, response);
        }

        return FunctionUtils.doAndHandle(() -> {
            val registeredService = (OidcRegisteredService) service;
            if (signingAndEncryptionService.shouldSignToken(registeredService)
                || signingAndEncryptionService.shouldEncryptToken(registeredService)) {
                return signAndEncryptUserProfileClaims(userProfile, response, registeredService);
            }
            return buildPlainUserProfileClaims(userProfile, response, registeredService);
        }, e -> ResponseEntity.badRequest().body("Unable to produce user profile claims")).get();
    }

    protected ResponseEntity<String> buildPlainUserProfileClaims(final Map<String, Object> userProfile,
                                                                 final HttpServletResponse response,
                                                                 final OidcRegisteredService registeredService) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        val claims = convertUserProfileIntoClaims(userProfile);
        return buildResponseEntity(claims.toJson(), response, registeredService);
    }

    private JwtClaims convertUserProfileIntoClaims(final Map<String, Object> userProfile) {
        val claims = new JwtClaims();
        userProfile.forEach((key, value) -> {
            if (OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES.equals(key)) {
                val attributes = (Map<String, Object>) value;
                val newAttributes = new HashMap<String, Object>();
                attributes.forEach((attrName, attrValue) -> newAttributes.put(attrName, determineAttributeValue(attrName, attrValue)));
                claims.setClaim(key, newAttributes);
            } else {
                claims.setClaim(key, determineAttributeValue(key, value));
            }
        });
        return claims;
    }

    protected ResponseEntity<String> signAndEncryptUserProfileClaims(final Map<String, Object> userProfile,
                                                                     final HttpServletResponse response,
                                                                     final OidcRegisteredService registeredService) {
        val claims = convertUserProfileIntoClaims(userProfile);
        claims.setAudience(registeredService.getClientId());
        claims.setIssuedAt(NumericDate.now());
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(signingAndEncryptionService.resolveIssuer(Optional.of(registeredService)));

        LOGGER.debug("Collected user profile claims, before cipher operations, are [{}]", claims);
        val result = this.signingAndEncryptionService.encode(registeredService, claims);
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
        headers.put("Content-Type", CollectionUtils.wrapList(response.getContentType()));
        return ResponseEntity.ok().headers(headers).body(result);
    }

    protected Object determineAttributeValue(final String name, final Object attrValue) {
        val values = CollectionUtils.toCollection(attrValue, ArrayList.class);
        val result = attributeDefinitionStore.locateAttributeDefinition(name, OidcAttributeDefinition.class);
        return result.map(defn -> defn.isSingleValue() && values.size() == 1 ? values.get(0) : attrValue)
            .orElseGet(() -> values.size() == 1 ? values.get(0) : values);
    }
}
