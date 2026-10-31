package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultOAuth20AccessTokenAuthenticator}, which accepts any access token that carries the required
 * scopes and builds the profile of its principal: the principal attributes, the authentication attributes and the
 * {@code client_id} of the token. The authentication handler name is {@link OAuth20UserProfile#COMMON_PROFILE_HANDLER_NAME}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@Getter
@Setter
public class DefaultOAuth20AccessTokenAuthenticator implements OAuth20AccessTokenAuthenticator {
    private Set<String> requiredScopes = new LinkedHashSet<>();

    @Override
    public Optional<OAuth20UserProfile> authenticate(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        return Optional.ofNullable(buildUserProfile(request, accessToken));
    }

    /**
     * Build the profile of the request from the access token.
     *
     * @param request     the request
     * @param accessToken the access token
     * @return the profile, or null when the token may not be used for this request
     */
    protected @Nullable OAuth20UserProfile buildUserProfile(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        val authentication = accessToken.getAuthentication();
        val principal = authentication.getPrincipal();
        val userProfile = new DefaultOAuth20UserProfile(principal.getId(), OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
        val attributes = new HashMap<String, Object>(principal.getAttributes());
        attributes.putAll(authentication.getAttributes());
        userProfile.addAttributes(attributes);
        userProfile.addAttribute(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        LOGGER.trace("Built user profile based on access token [{}] is [{}]", accessToken, userProfile);
        return userProfile;
    }
}
