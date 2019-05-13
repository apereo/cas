package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
    private final OAuthProperties oauthProperties;

    @Override
    public String render(final Map<String, Object> model, final AccessToken accessToken) {
        val userProfile = getRenderedUserProfile(model, accessToken);
        return renderProfileForModel(userProfile, accessToken);
    }

    /**
     * Render profile for model string.
     *
     * @param userProfile the user profile
     * @param accessToken the access token
     * @return the string
     */
    protected String renderProfileForModel(final Map<String, Object> userProfile, final AccessToken accessToken) {
        val json = OAuth20Utils.toJson(userProfile);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Final user profile is [{}]", json);
        }
        return json;
    }

    /**
     * Gets rendered user profile.
     *
     * @param model       the model
     * @param accessToken the access token
     * @return the rendered user profile
     */
    protected Map<String, Object> getRenderedUserProfile(final Map<String, Object> model, final AccessToken accessToken) {
        if (oauthProperties.getUserProfileViewType() == OAuthProperties.UserProfileViewTypes.FLAT) {
            val flattened = new LinkedHashMap<String, Object>();
            if (model.containsKey(MODEL_ATTRIBUTE_ATTRIBUTES)) {
                val attributes = Map.class.cast(model.get(MODEL_ATTRIBUTE_ATTRIBUTES));
                flattened.putAll(attributes);
            }
            model.keySet()
                .stream()
                .filter(k -> !k.equalsIgnoreCase(MODEL_ATTRIBUTE_ATTRIBUTES))
                .forEach(k -> flattened.put(k, model.get(k)));
            LOGGER.trace("Flattened user profile attributes with the final model as [{}]", model);
            return flattened;
        }
        return model;
    }
}
