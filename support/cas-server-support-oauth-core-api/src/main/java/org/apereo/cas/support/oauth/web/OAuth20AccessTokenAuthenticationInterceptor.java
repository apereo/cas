package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20AccessTokenAuthenticator;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ProofOfPossessionValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Protects an endpoint with access tokens (RFC 6750): OIDC dynamic client registration and client configuration, and
 * the UMA protection and authorization APIs. The token is read from the {@code Authorization} header, under the
 * {@code Bearer} or, for a DPoP-bound token, the {@code DPoP} scheme. A request without a token is answered with
 * {@code 401} and a bare challenge; an unknown or expired token, or one the authenticator does not accept for the request
 * or fails on, with {@code 401} and {@code invalid_token}; a token without the required scopes with {@code 403} and
 * {@code insufficient_scope} (RFC 6750, section 3.1). A sender-constrained token is accepted only with its DPoP proof or
 * over a connection that presents its certificate (RFC 9449, section 7; RFC 8705, section 3). The profile the
 * authenticator builds is kept for the request, where {@link OAuth20Utils#findAuthenticatedUserProfile} finds it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20AccessTokenAuthenticationInterceptor implements HandlerInterceptor {
    private final OAuth20AccessTokenAuthenticator authenticator;

    private final TicketRegistry ticketRegistry;

    private final JwtBuilder accessTokenJwtBuilder;

    private final OAuth20ProofOfPossessionValidator proofOfPossessionValidator;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        OAuth20Utils.addSecurityHeaders(request, response);
        val presentedAccessToken = OAuth20Utils.extractAccessTokenFromAuthorizationHeader(request).orElse(null);
        if (presentedAccessToken == null) {
            LOGGER.debug("Request to [{}] carries no access token", request.getRequestURI());
            return reject(request, response, HttpStatus.UNAUTHORIZED, null, null, null);
        }
        val accessToken = resolveAccessToken(presentedAccessToken);
        if (accessToken == null) {
            return reject(request, response, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_TOKEN,
                "The access token is unknown or has expired", null);
        }
        val proofOfPossessionError = OAuth20Utils.verifyProofOfPossession(request, response,
            presentedAccessToken, accessToken, proofOfPossessionValidator);
        if (proofOfPossessionError != null) {
            return writeResponse(response, proofOfPossessionError);
        }
        val requiredScopes = authenticator.getRequiredScopes();
        if (!accessToken.getScopes().containsAll(requiredScopes)) {
            LOGGER.debug("Access token of client [{}] does not carry the required scopes [{}]", accessToken.getClientId(), requiredScopes);
            return reject(request, response, HttpStatus.FORBIDDEN, OAuth20Constants.INSUFFICIENT_SCOPE,
                "The access token does not carry the required scope", String.join(" ", requiredScopes));
        }
        val profile = authenticateAccessToken(request, accessToken);
        if (profile.isEmpty()) {
            return reject(request, response, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_TOKEN,
                "The access token may not be used for this request", null);
        }
        LOGGER.debug("Access token of client [{}] authenticated [{}]", accessToken.getClientId(), profile.get().getId());
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile.get());
        return true;
    }

    /**
     * The profile the authenticator builds for the access token; empty when it refuses the token or fails on it.
     *
     * @param request     the request
     * @param accessToken the access token
     * @return the profile
     */
    protected Optional<OAuth20UserProfile> authenticateAccessToken(final HttpServletRequest request, final OAuth20AccessToken accessToken) {
        try {
            return authenticator.authenticate(request, accessToken);
        } catch (final Exception e) {
            LOGGER.warn("Unable to authenticate the access token of client [{}]: [{}]", accessToken.getClientId(), e.getMessage());
            LOGGER.debug(e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * The access token behind the presented value, which is the ticket id or, for a token issued as a JWT, the JWT;
     * null when it is unknown, of another type or expired.
     *
     * @param presentedAccessToken the access token as the client presented it
     * @return the access token, or null
     */
    protected @Nullable OAuth20AccessToken resolveAccessToken(final String presentedAccessToken) {
        try {
            val tokenId = OAuth20JwtAccessTokenEncoder.toDecodableCipher(accessTokenJwtBuilder).decode(presentedAccessToken);
            if (StringUtils.isBlank(tokenId)) {
                return null;
            }
            val accessToken = ticketRegistry.getTicket(tokenId, OAuth20AccessToken.class);
            return accessToken == null || accessToken.isExpired() ? null : accessToken;
        } catch (final Exception e) {
            LOGGER.debug("Unable to find the presented access token: [{}]", e.getMessage());
            return null;
        }
    }

    private boolean reject(final HttpServletRequest request, final HttpServletResponse response, final HttpStatus status,
                           final @Nullable String error, final @Nullable String description, final @Nullable String scope)
            throws IOException {
        return writeResponse(response, OAuth20Utils.protectedResourceErrorResponse(request, status, error, description, scope,
            proofOfPossessionValidator.getAcceptedSigningAlgorithms()));
    }

    private static boolean writeResponse(final HttpServletResponse response, final ResponseEntity<?> entity) throws IOException {
        response.setStatus(entity.getStatusCode().value());
        entity.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        val body = entity.getBody();
        if (body != null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (val writer = response.getWriter()) {
                writer.write(OAuth20Utils.toJson(body));
            }
        }
        return false;
    }
}
