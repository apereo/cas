package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link BaseAccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Slf4j
public abstract class BaseAccessTokenGrantRequestExtractor<T extends OAuth20ConfigurationContext> implements AccessTokenGrantRequestExtractor {
    private final ObjectProvider<@NonNull T> configurationContext;

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

    /**
     * The requested scope MUST NOT include any scope
     * not originally granted by the resource owner, and if omitted is
     * treated as equal to the scope originally granted by the
     * resource owner.
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param context         the context
     * @return scopes
     */
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes,
                                                        final OAuth20Token token,
                                                        final WebContext context) {
        if (requestedScopes.isEmpty()) {
            return new TreeSet<>(token.getScopes());
        }
        if (!token.getScopes().containsAll(requestedScopes)) {
            LOGGER.error("Requested scopes [{}] exceed the granted scopes [{}] for token [{}]",
                requestedScopes, token.getScopes(), token.getId());
            throw new OAuth20UnauthorizedScopeRequestException(token.getId());
        }
        return new TreeSet<>(requestedScopes);
    }

    protected abstract AccessTokenRequestContext extractRequest(WebContext webContext) throws Throwable;
}
