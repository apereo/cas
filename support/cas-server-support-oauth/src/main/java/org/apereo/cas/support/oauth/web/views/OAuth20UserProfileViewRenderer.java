package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.ticket.accesstoken.AccessToken;

import java.util.Map;

/**
 * This is {@link OAuth20UserProfileViewRenderer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface OAuth20UserProfileViewRenderer {

    /**
     * Render.
     *
     * @param model       the model
     * @param accessToken the access token
     * @return the string
     */
    String render(Map<String, Object> model, AccessToken accessToken);
}
