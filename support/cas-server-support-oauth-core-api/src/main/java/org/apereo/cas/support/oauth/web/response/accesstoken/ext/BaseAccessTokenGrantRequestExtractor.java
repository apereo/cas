package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Optional;

/**
 * This is {@link BaseAccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseAccessTokenGrantRequestExtractor<T extends OAuth20ConfigurationContext> implements AccessTokenGrantRequestExtractor {
    private final ObjectProvider<T> configurationContext;

    @Override
    public AccessTokenRequestContext extract(final WebContext webContext) throws Throwable {
        val tokenRequestContext = extractRequest(webContext);
        extractUserProfile(webContext).ifPresent(profile -> {
            if (profile.containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)) {
                tokenRequestContext.setDpopConfirmation(profile.getAttribute(OAuth20Constants.DPOP_CONFIRMATION).toString());
            }
            if (profile.containsAttribute(OAuth20Constants.DPOP)) {
                tokenRequestContext.setDpop(profile.getAttribute(OAuth20Constants.DPOP).toString());
            }
            tokenRequestContext.setUserProfile(profile);
        });
        return tokenRequestContext;
    }

    protected Optional<UserProfile> extractUserProfile(final WebContext webContext) {
        return new ProfileManager(webContext, configurationContext.getObject().getSessionStore()).getProfile();
    }

    protected abstract AccessTokenRequestContext extractRequest(WebContext webContext) throws Throwable;
}
