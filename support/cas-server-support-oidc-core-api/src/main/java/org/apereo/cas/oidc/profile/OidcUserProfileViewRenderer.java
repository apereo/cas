package org.apereo.cas.oidc.profile;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.OAuth20TokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    private final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService;

    public OidcUserProfileViewRenderer(final OAuthProperties oauthProperties,
                                       final ServicesManager servicesManager,
                                       final OAuth20TokenSigningAndEncryptionService signingAndEncryptionService) {
        super(servicesManager, oauthProperties);
        this.signingAndEncryptionService = signingAndEncryptionService;
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
            return buildPlainUserProfileClaims(userProfile, response);
        }, e -> ResponseEntity.badRequest().body("Unable to produce user profile claims")).get();
    }

    protected ResponseEntity<String> buildPlainUserProfileClaims(final Map<String, Object> userProfile,
                                                                 final HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        val result = MAPPER.writeValueAsString(userProfile);
        return ResponseEntity.ok(result);
    }

    protected ResponseEntity<String> signAndEncryptUserProfileClaims(final Map<String, Object> userProfile,
                                                                     final HttpServletResponse response,
                                                                     final OidcRegisteredService registeredService) {
        val claims = new JwtClaims();
        userProfile.forEach((key, value) -> {
            if (OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES.equals(key)) {
                val attributes = (Map<String, Object>) value;
                val newAttributes = new HashMap<String, Object>();
                attributes.forEach((k, v) -> newAttributes.put(k, useSingleValueForSingletonList(v)));
                claims.setClaim(key, newAttributes);
            } else {
                claims.setClaim(key, useSingleValueForSingletonList(value));
            }
        });
        claims.setAudience(registeredService.getClientId());
        claims.setIssuedAt(NumericDate.now());
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setIssuer(signingAndEncryptionService.resolveIssuer(Optional.of(registeredService)));

        LOGGER.debug("Collected user profile claims, before cipher operations, are [{}]", claims);
        val result = this.signingAndEncryptionService.encode(registeredService, claims);
        LOGGER.debug("Finalized user profile is [{}]", result);

        response.setContentType(OidcConstants.CONTENT_TYPE_JWT);
        val headers = new HttpHeaders();
        headers.put("Content-Type", CollectionUtils.wrapList(OidcConstants.CONTENT_TYPE_JWT));
        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    private static Object useSingleValueForSingletonList(final Object value) {
        if (value instanceof Collection && ((Collection) value).size() == 1) {
            return ((Collection) value).iterator().next();
        }
        return value;
    }
}
