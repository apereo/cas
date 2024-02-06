package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
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

    private Set<String> requiredScopes = new LinkedHashSet<>();

    protected String extractAccessTokenFrom(final TokenCredentials tokenCredentials) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder).decode(tokenCredentials.getToken());
    }

    @Override
    public Optional<Credentials> validate(final CallContext callContext, final Credentials credentials) {
        val tokenCredentials = (TokenCredentials) credentials;
        val token = extractAccessTokenFrom(tokenCredentials);
        LOGGER.trace("Received access token [{}] for authentication", token);

        val accessToken = ticketRegistry.getTicket(token, OAuth20AccessToken.class);
        if (accessToken == null || accessToken.isExpired()) {
            LOGGER.error("Provided access token [{}] is either not found in the ticket registry or has expired", token);
            return Optional.empty();
        }

        if (!requiredScopes.isEmpty() && !accessToken.getScopes().containsAll(requiredScopes)) {
            LOGGER.error("Unable to authenticate access token without required scopes [{}]", requiredScopes);
            return Optional.empty();
        }

        val profile = buildUserProfile(tokenCredentials, callContext, accessToken);
        if (profile != null) {
            LOGGER.trace("Final user profile based on access token [{}] is [{}]", accessToken, profile);
            tokenCredentials.setUserProfile(profile);
            return Optional.of(tokenCredentials);
        }
        return Optional.empty();
    }

    protected CommonProfile buildUserProfile(final TokenCredentials tokenCredentials,
                                             final CallContext callContext,
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
