package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.hjson.Stringify;

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
        val value = getRenderedUserProfile(model);
        LOGGER.debug("Final user profile is [{}]", JsonValue.readHjson(value).toString(Stringify.FORMATTED));
        return value;
    }

    /**
     * Gets rendered user profile.
     *
     * @param model the model
     * @return the rendered user profile
     */
    protected String getRenderedUserProfile(final Map<String, Object> model) {
        if (oauthProperties.getUserProfileViewType() == OAuthProperties.UserProfileViewTypes.FLAT) {
            val flattened = new LinkedHashMap<>();
            if (model.containsKey(MODEL_ATTRIBUTE_ATTRIBUTES)) {
                val attributes = Map.class.cast(model.get(MODEL_ATTRIBUTE_ATTRIBUTES));
                flattened.putAll(attributes);
            }
            model.keySet()
                .stream()
                .filter(k -> !k.equalsIgnoreCase(MODEL_ATTRIBUTE_ATTRIBUTES))
                .forEach(k -> flattened.put(k, model.get(k)));
            return OAuth20Utils.jsonify(flattened);
        }
        return OAuth20Utils.jsonify(model);

    }
}
