package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

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
     * Bean name of {@link OAuth20UserProfileDataCreator}.
     */
    String BEAN_NAME = "oauth2UserProfileDataCreator";

    /**
     * Create internal user profile data.
     *
     * @param accessToken oauth access token
     * @return Map representing profile data
     * @throws Throwable the throwable
     */
    Map<String, Object> createFrom(OAuth20AccessToken accessToken) throws Throwable;
}
