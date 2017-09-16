package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link OAuth20DefaultUserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20DefaultUserProfileViewRenderer implements OAuth20UserProfileViewRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20DefaultUserProfileViewRenderer.class);
    private final OAuthProperties oauthProperties;

    public OAuth20DefaultUserProfileViewRenderer(final OAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    @Override
    public String render(final Map<String, Object> model, final AccessToken accessToken) {
        final String value;

        switch (oauthProperties.getUserProfileViewType()) {
            case FLAT:
                final Map<String, Object> flattened = new LinkedHashMap<>();
                if (model.containsKey(MODEL_ATTRIBUTE_ATTRIBUTES)) {
                    final Map attributes = Map.class.cast(model.get(MODEL_ATTRIBUTE_ATTRIBUTES));
                    flattened.putAll(attributes);
                }
                model.keySet()
                        .stream()
                        .filter(k -> !k.equalsIgnoreCase(MODEL_ATTRIBUTE_ATTRIBUTES))
                        .forEach(k -> flattened.put(k, model.get(k)));
                value = OAuth20Utils.jsonify(flattened);
                break;
            case NESTED:
            default:
                value = OAuth20Utils.jsonify(model);
                break;
        }

        LOGGER.debug("Final user profile is [{}]", JsonValue.readHjson(value).toString(Stringify.FORMATTED));
        return value;
    }
}
