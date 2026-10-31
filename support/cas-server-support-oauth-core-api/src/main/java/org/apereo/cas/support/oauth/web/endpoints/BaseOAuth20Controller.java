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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
        getConfigurationContext().configureSessionReplicationCookiePath(request);
    }

    protected boolean isRequestAuthenticated(final HttpServletRequest request, final HttpServletResponse response,
                                             @Nullable final OAuthRegisteredService registeredService) {
        return OAuth20Utils.findAuthenticatedUserProfile(request, response, getConfigurationContext().getSessionStore()).isPresent();
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
     * The access token carried by the authorization header, under the {@code Bearer} or {@code DPoP} scheme.
     *
     * @param request the request
     * @return the access token carried by the authorization header, if any
     * @see OAuth20Utils#extractAccessTokenFromAuthorizationHeader(HttpServletRequest)
     */
    protected Optional<String> extractAccessTokenFromAuthorizationHeader(final HttpServletRequest request) {
        return OAuth20Utils.extractAccessTokenFromAuthorizationHeader(request);
    }

    /**
     * The authentication scheme the client used to present its access token.
     *
     * @param request the request
     * @return the authentication scheme
     * @see OAuth20Utils#resolveAuthorizationScheme(HttpServletRequest)
     */
    protected String resolveAuthorizationScheme(final HttpServletRequest request) {
        return OAuth20Utils.resolveAuthorizationScheme(request);
    }

    /**
     * A {@code 401} with a {@code WWW-Authenticate} challenge in the scheme the client used.
     *
     * @param request     the request
     * @param error       the error code, or null when the request carried no token at all
     * @param description the error description
     * @return the response entity
     * @see OAuth20Utils#protectedResourceErrorResponse
     */
    protected ResponseEntity unauthorized(final HttpServletRequest request,
                                          final @Nullable String error,
                                          final @Nullable String description) {
        return OAuth20Utils.protectedResourceErrorResponse(request, HttpStatus.UNAUTHORIZED, error, description, null,
            configurationContext.getProofOfPossessionValidator().getAcceptedSigningAlgorithms());
    }

    /**
     * Verify the DPoP proof or the certificate binding of a request to a protected resource, when the access token is
     * sender-constrained.
     *
     * @param request              the request
     * @param response             the response
     * @param presentedAccessToken the access token exactly as the client presented it
     * @param accessToken          the access token ticket
     * @return an error response when the proof is missing or does not verify, otherwise null
     * @see OAuth20Utils#verifyProofOfPossession
     */
    protected @Nullable ResponseEntity verifyProofOfPossession(final HttpServletRequest request,
                                                               final HttpServletResponse response,
                                                               final String presentedAccessToken,
                                                               final OAuth20AccessToken accessToken) {
        return OAuth20Utils.verifyProofOfPossession(request, response, presentedAccessToken, accessToken,
            configurationContext.getProofOfPossessionValidator());
    }

    /**
     * Remove what would end a quoted challenge parameter early.
     *
     * @param value the value
     * @return the sanitized value
     * @see OAuth20Utils#toChallengeValue(String)
     */
    protected static String toChallengeValue(final String value) {
        return OAuth20Utils.toChallengeValue(value);
    }
}
