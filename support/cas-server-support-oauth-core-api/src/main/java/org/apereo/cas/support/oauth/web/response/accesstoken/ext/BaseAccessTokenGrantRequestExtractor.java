package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    private final ObjectProvider<T> configurationContext;

    /**
     * The DPoP proof that binds the token comes from the authenticated profile, or, for grants whose client is not that
     * profile (device code polling, the JWT bearer grant), from the request attributes that
     * {@code OAuth20ProofOfPossessionValidator.validateTokenRequest} sets. The request attributes always belong to the
     * current request, so they win over a profile that may have been kept in the session from an earlier one. A client
     * that authenticated with mutual TLS leaves the thumbprint of its certificate on the profile, and the token is bound
     * to that certificate (RFC 8705, section 3).
     *
     * @param request  the request
     * @param response the response
     * @return the access token request context
     * @throws Throwable the throwable
     */
    @Override
    public AccessTokenRequestContext extract(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val tokenRequestContext = extractRequest(request, response);
        extractUserProfile(request, response).ifPresent(profile -> {
            Optional.ofNullable(profile.getAttribute(OAuth20Constants.DPOP_CONFIRMATION))
                .ifPresent(confirmation -> tokenRequestContext.setDpopConfirmation(confirmation.toString()));
            Optional.ofNullable(profile.getAttribute(OAuth20Constants.DPOP))
                .ifPresent(proof -> tokenRequestContext.setDpop(proof.toString()));
            Optional.ofNullable(profile.getAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST))
                .ifPresent(digest -> tokenRequestContext.setX509CertificateDigest(digest.toString()));
            tokenRequestContext.setUserProfile(profile);
        });
        Optional.ofNullable(request.getAttribute(OAuth20Constants.DPOP_CONFIRMATION))
            .ifPresent(confirmation -> tokenRequestContext.setDpopConfirmation(confirmation.toString()));
        Optional.ofNullable(request.getAttribute(OAuth20Constants.DPOP))
            .ifPresent(proof -> tokenRequestContext.setDpop(proof.toString()));
        return tokenRequestContext;
    }

    protected Optional<OAuth20UserProfile> extractUserProfile(final HttpServletRequest request, final HttpServletResponse response) {
        return OAuth20Utils.findAuthenticatedUserProfile(request, response, configurationContext.getObject().getSessionStore());
    }

    /**
     * The requested scope MUST NOT include any scope
     * not originally granted by the resource owner, and if omitted is
     * treated as equal to the scope originally granted by the
     * resource owner.
     *
     * @param requestedScopes the requested scopes
     * @param token           the token
     * @param request         the request
     * @return scopes
     */
    protected Set<String> extractRequestedScopesByToken(final Set<String> requestedScopes,
                                                        final OAuth20Token token,
                                                        final HttpServletRequest request) {
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

    protected abstract AccessTokenRequestContext extractRequest(HttpServletRequest request, HttpServletResponse response) throws Throwable;
}
