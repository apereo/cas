package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link OAuth20AccessTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class OAuth20AccessTokenAuthenticator implements Authenticator {
    private final TicketRegistry ticketRegistry;

    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    private Set<String> requiredScopes = new LinkedHashSet<>();

    private String extractAccessTokenFrom(final TokenCredentials tokenCredentials) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .profileScopeToAttributesFilter(profileScopeToAttributesFilter)
            .build()
            .decode(tokenCredentials.getToken());
    }

    @Override
    public void validate(final Credentials credentials, final WebContext webContext, final SessionStore sessionStore) {
        val tokenCredentials = (TokenCredentials) credentials;
        val token = extractAccessTokenFrom(tokenCredentials);
        LOGGER.trace("Received access token [{}] for authentication", token);

        val accessToken = ticketRegistry.getTicket(token, OAuth20AccessToken.class);
        if (accessToken == null || accessToken.isExpired()) {
            LOGGER.error("Provided access token [{}] is either not found in the ticket registry or has expired", token);
            return;
        }

        if (!requiredScopes.isEmpty() && !accessToken.getScopes().containsAll(requiredScopes)) {
            LOGGER.error("Unable to authenticate access token without required scopes [{}]", requiredScopes);
            return;
        }

        val profile = buildUserProfile(tokenCredentials, webContext, accessToken);
        if (profile != null) {
            LOGGER.trace("Final user profile based on access token [{}] is [{}]", accessToken, profile);
            tokenCredentials.setUserProfile(profile);
        }
    }

    /**
     * Build user profile common profile.
     *
     * @param tokenCredentials the token credentials
     * @param webContext       the web context
     * @param accessToken      the access token
     * @return the common profile
     */
    protected CommonProfile buildUserProfile(final TokenCredentials tokenCredentials,
                                             final WebContext webContext,
                                             final OAuth20AccessToken accessToken) {
        val userProfile = new CommonProfile(true);
        val authentication = accessToken.getAuthentication();
        val principal = authentication.getPrincipal();

        userProfile.setId(principal.getId());
        val attributes = new HashMap<String, Object>(principal.getAttributes());
        attributes.putAll(authentication.getAttributes());
        userProfile.addAttributes(attributes);
        userProfile.addAttribute(OAuth20Constants.CLIENT_ID, accessToken.getClientId());

        LOGGER.trace("Built user profile based on access token [{}] is [{}]", accessToken, userProfile);
        return userProfile;
    }
}
