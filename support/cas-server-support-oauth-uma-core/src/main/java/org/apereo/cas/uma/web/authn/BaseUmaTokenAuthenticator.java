package org.apereo.cas.uma.web.authn;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

import java.util.LinkedHashMap;

/**
 * This is {@link BaseUmaTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseUmaTokenAuthenticator implements Authenticator {
    private final CentralAuthenticationService centralAuthenticationService;

    private final JwtBuilder accessTokenJwtBuilder;

    @Override
    public void validate(final Credentials creds, final WebContext webContext, final SessionStore sessionStore) {
        val credentials = (TokenCredentials) creds;
        val token = extractAccessTokenFrom(credentials.getToken().trim());
        val at = this.centralAuthenticationService.getTicket(token, OAuth20AccessToken.class);
        if (!at.getScopes().contains(getRequiredScope())) {
            val err = String.format("Missing scope [%s]. Unable to authenticate access token %s", getRequiredScope(), token);
            throw new CredentialsException(err);
        }
        val profile = new CommonProfile();
        val authentication = at.getAuthentication();
        val principal = authentication.getPrincipal();
        profile.setId(principal.getId());
        val attributes = new LinkedHashMap<String, Object>(authentication.getAttributes());
        attributes.putAll(principal.getAttributes());

        profile.addAttributes(attributes);
        profile.addPermissions(at.getScopes());
        profile.addAttribute(OAuth20AccessToken.class.getName(), at);

        LOGGER.debug("Authenticated access token [{}]", profile);
        credentials.setUserProfile(profile);
    }

    /**
     * Extract access token from token.
     *
     * @param token the token
     * @return the string
     */
    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(accessTokenJwtBuilder)
            .build()
            .decode(token);
    }

    /**
     * Gets required scope.
     *
     * @return the required scope
     */
    protected abstract String getRequiredScope();
}
