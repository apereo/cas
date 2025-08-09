package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.configuration.model.support.oauth.OAuthCoreProperties;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20AttributeDefinition;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DefaultUserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultUserProfileViewRenderer implements OAuth20UserProfileViewRenderer {
    protected final ServicesManager servicesManager;

    private final OAuthProperties oauthProperties;

    private final AttributeDefinitionStore attributeDefinitionStore;
    
    @Override
    public ResponseEntity render(final Map<String, Object> model,
                                 final OAuth20AccessToken accessToken,
                                 final HttpServletResponse response) {
        val userProfile = getRenderedUserProfile(model, accessToken, response);
        return renderProfileForModel(userProfile, accessToken, response);
    }

    protected ResponseEntity renderProfileForModel(final Map<String, Object> userProfile,
                                                   final OAuth20AccessToken accessToken,
                                                   final HttpServletResponse response) {
        val claims = convertUserProfileIntoClaims(userProfile);
        return new ResponseEntity<>(claims.getClaimsMap(), HttpStatus.OK);
    }

    protected Map<String, Object> getRenderedUserProfile(final Map<String, Object> model,
                                                         final OAuth20AccessToken accessToken,
                                                         final HttpServletResponse response) {
        val type = determineUserProfileType(accessToken);
        LOGGER.debug("User profile view type for client [{}] is set to [{}]", accessToken.getClientId(), type);
        if (type == OAuthCoreProperties.UserProfileViewTypes.FLAT) {
            return flattenUserProfile(model);
        }
        return model;
    }

    protected OAuthCoreProperties.UserProfileViewTypes determineUserProfileType(final OAuth20AccessToken accessToken) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, accessToken.getClientId());
        return registeredService != null && registeredService.getUserProfileViewType() != null
            ? registeredService.getUserProfileViewType()
            : oauthProperties.getCore().getUserProfileViewType();
    }

    protected Map<String, Object> flattenUserProfile(final Map<String, Object> model) {
        val flattened = new LinkedHashMap<String, Object>();
        if (model.containsKey(MODEL_ATTRIBUTE_ATTRIBUTES)) {
            val attributes = (Map) model.get(MODEL_ATTRIBUTE_ATTRIBUTES);
            flattened.putAll(attributes);
        }
        model.keySet()
            .stream()
            .filter(attributeName -> !attributeName.equalsIgnoreCase(MODEL_ATTRIBUTE_ATTRIBUTES))
            .forEach(attributeName -> flattened.put(attributeName, model.get(attributeName)));
        LOGGER.trace("Flattened user profile attributes with the final model as [{}]", model);
        return flattened;
    }


    protected JwtClaims convertUserProfileIntoClaims(final Map<String, Object> userProfile) {
        val claims = new JwtClaims();
        userProfile
            .entrySet()
            .stream()
            .filter(entry -> !entry.getKey().startsWith(CentralAuthenticationService.NAMESPACE))
            .forEach(entry -> {
                if (OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES.equals(entry.getKey())) {
                    val attributes = (Map<String, Object>) entry.getValue();
                    val newAttributes = new HashMap<String, Object>();
                    attributes.forEach((attrName, attrValue) -> newAttributes.put(attrName, determineAttributeValue(attrName, attrValue)));
                    claims.setClaim(entry.getKey(), newAttributes);
                } else {
                    claims.setClaim(entry.getKey(), determineAttributeValue(entry.getKey(), entry.getValue()));
                }
            });
        return claims;
    }

    protected Object determineAttributeValue(final String name, final Object attrValue) {
        val values = CollectionUtils.toCollection(attrValue, ArrayList.class);
        val result = attributeDefinitionStore.locateAttributeDefinition(name, OAuth20AttributeDefinition.class);
        return result.map(defn -> defn.toAttributeValue(values))
            .orElseGet(() -> values.size() == 1 ? values.getFirst() : values);
    }
}
