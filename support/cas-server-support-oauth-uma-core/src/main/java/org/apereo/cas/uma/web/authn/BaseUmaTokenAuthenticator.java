package org.apereo.cas.uma.web.authn;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.DefaultOAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseUmaTokenAuthenticator}, which accepts an access token with the scope of an UMA API and builds a
 * profile whose roles are the scopes of the token and which carries the token itself.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public abstract class BaseUmaTokenAuthenticator extends DefaultOAuth20AccessTokenAuthenticator {

    @Override
    public Set<String> getRequiredScopes() {
        return Set.of(getRequiredScope());
    }

    @Override
    protected OAuth20UserProfile buildUserProfile(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        val authentication = accessToken.getAuthentication();
        val principal = authentication.getPrincipal();
        val profile = new DefaultOAuth20UserProfile(principal.getId(), OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
        val attributes = new LinkedHashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(principal.getAttributes());
        profile.addAttributes(attributes);
        profile.addRoles(accessToken.getScopes());
        profile.addAttribute(OAuth20AccessToken.class.getName(), accessToken);
        profile.addAttribute(OAuth20Constants.CLIENT_ID, accessToken.getClientId());
        LOGGER.debug("Authenticated access token [{}]", profile);
        return profile;
    }

    protected abstract String getRequiredScope();
}
