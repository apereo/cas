package org.apereo.cas.support.oauth.web.views;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;
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
@AllArgsConstructor
public class OAuth20DefaultUserProfileViewRenderer implements OAuth20UserProfileViewRenderer {
    private final OAuthProperties oauthProperties;


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
