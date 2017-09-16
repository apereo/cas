package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This is {@link OAuth20DefaultUserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20DefaultUserProfileViewRenderer implements OAuth20UserProfileViewRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20DefaultUserProfileViewRenderer.class);

    @Override
    public String render(final Map<String, Object> model, final AccessToken accessToken) {
        final String value = OAuth20Utils.jsonify(model);
        LOGGER.debug("Final user profile is [{}]", JsonValue.readHjson(value).toString(Stringify.FORMATTED));
        return value;
    }
}
