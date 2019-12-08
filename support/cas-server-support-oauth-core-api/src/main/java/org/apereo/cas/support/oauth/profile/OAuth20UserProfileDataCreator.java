package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import org.pac4j.core.context.JEEContext;

import java.util.Map;

/**
 * Strategy interface that acts as factory to handle creation of un-typed user profile data which then could be transformed into whatever
 * representation necessary by upstream components.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@FunctionalInterface
public interface OAuth20UserProfileDataCreator {

    /**
     * Create internal user profile data.
     *
     * @param accessToken oauth access token
     * @param context  request context
     * @return Map representing profile data
     */
    Map<String, Object> createFrom(OAuth20AccessToken accessToken, JEEContext context);
}
