package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.pac4j.core.context.J2EContext;

import java.util.Map;

/**
 * Default implementation of {@link OAuth2UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class DefaultOAuth2UserProfileDataCreator implements OAuth2UserProfileDataCreator {

    @Override
    public Map<String, Object> createProfileDataFrom(AccessToken accessToken, J2EContext j2EContext) {
        return null;
    }
}
