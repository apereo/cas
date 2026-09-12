package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.web.AbstractController;
import org.apereo.cas.web.support.CookieUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import jakarta.servlet.http.HttpServletRequest;


/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Slf4j
public abstract class BaseOAuth20Controller<T extends OAuth20ConfigurationContext> extends AbstractController {
    protected final T configurationContext;

    protected @Nullable OAuth20AccessToken resolveAccessToken(final Ticket givenAccessToken) {
        return resolveToken(givenAccessToken, OAuth20AccessToken.class);
    }

    protected @Nullable <U extends Ticket> U resolveToken(final Ticket token, final Class<U> clazz) {
        return token.isStateless()
            ? configurationContext.getTicketRegistry().getTicket(token.getId(), clazz)
            : clazz.cast(token);
    }

    protected @Nullable String extractAccessTokenFrom(final String token) {
        val decodableCipher = OAuth20JwtAccessTokenEncoder.toDecodableCipher(getConfigurationContext().getAccessTokenJwtBuilder());
        return decodableCipher.decode(token);
    }

    protected void ensureSessionReplicationIsAutoconfiguredIfNeedBe(final HttpServletRequest request) {
        val replicationProps = getConfigurationContext().getCasProperties().getAuthn().getOauth().getSessionReplication();
        val cookieAutoconfigured = replicationProps.getCookie().isAutoConfigureCookiePath();
        if (replicationProps.isReplicateSessions() && cookieAutoconfigured) {
            val cookieBuilder = getConfigurationContext().getOauthDistributedSessionCookieGenerator();
            CookieUtils.configureCookiePath(request, cookieBuilder);
        }
    }

    protected boolean isRequestAuthenticated(final ProfileManager manager, final WebContext context,
                                             @Nullable final OAuthRegisteredService registeredService) {
        return manager.getProfile().isPresent();
    }

    protected @Nullable OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Determines whether the OAuth token is a refresh token.
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    protected static boolean isRefreshToken(final OAuth20Token token) {
        return token instanceof OAuth20RefreshToken;
    }

    /**
     * Determines whether the OAuth token is an access token.
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    protected static boolean isAccessToken(final OAuth20Token token) {
        return token instanceof OAuth20AccessToken;
    }

    protected void revokeToken(final OAuth20RefreshToken token) throws Exception {
        LOGGER.debug("Revoking refresh token [{}] and all associated access tokens", token.getId());
        token.getAccessTokens().removeIf(Unchecked.predicate(this::revokeToken));
        revokeToken(token.getId());
    }

    protected boolean revokeToken(final String token) throws Exception {
        LOGGER.debug("Revoking token [{}]", token);
        return getConfigurationContext().getTicketRegistry().deleteTicket(token) > 0;
    }

    protected Pair<String, String> getAccessTokenFromRequest(final HttpServletRequest request) {
        var accessToken = StringUtils.defaultIfBlank(
            request.getParameter(OAuth20Constants.ACCESS_TOKEN),
            request.getParameter(OAuth20Constants.TOKEN));
        if (StringUtils.isBlank(accessToken)) {
            accessToken = extractAccessTokenFromAuthorizationHeader(request).orElse(accessToken);
        }
        LOGGER.debug("[{}]: [{}]", OAuth20Constants.ACCESS_TOKEN, accessToken);
        return Pair.of(accessToken, extractAccessTokenFrom(accessToken));
    }

    /**
     * Access tokens reach a protected resource under an authentication scheme, and the scheme that
     * applies depends on how the token was bound. Plain tokens use {@code Bearer} per RFC 6750,
     * while sender-constrained tokens use {@code DPoP}: RFC 9449, section 7.1 states that "a
     * DPoP-bound access token is sent using the Authorization request header field ... with an
     * authentication scheme of DPoP". CAS answers the token request with {@code token_type: DPoP}
     * whenever a proof accompanied it, so every protected resource here has to accept that scheme
     * back; recognizing {@code Bearer} alone would reject the very tokens CAS just minted.
     *
     * @param request the request
     * @return the access token carried by the authorization header, if any
     */
    protected Optional<String> extractAccessTokenFromAuthorizationHeader(final HttpServletRequest request) {
        val authHeader = request.getHeader(HttpConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isBlank(authHeader)) {
            return Optional.empty();
        }
        return Stream.of(OAuth20Constants.TOKEN_TYPE_BEARER, OAuth20Constants.TOKEN_TYPE_DPOP)
            .filter(scheme -> StringUtils.startsWithIgnoreCase(authHeader, scheme + ' '))
            .findFirst()
            .map(scheme -> StringUtils.trimToNull(authHeader.substring(scheme.length() + 1)));
    }
}
