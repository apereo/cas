package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationProvider;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationResponseException;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticationToken;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates the client of token, revocation, pushed authorization, backchannel authentication and verifiable
 * credential transaction requests. The client authentication methods are tried in order, and the first that
 * authenticates the request wins; its profile is kept for the request, where
 * {@link OAuth20Utils#findAuthenticatedUserProfile} finds it. A method only authenticates the request when the client it
 * authenticated is the client that the request names, as the rest of the request is processed for that client (RFC 6749,
 * section 2.3), and a method that fails with an error is a failed method. A request that no method authenticates is refused with
 * {@code 401} and a {@code WWW-Authenticate} header that asks for HTTP Basic credentials, the authentication scheme of client
 * authentication (RFC 6749, section 5.2); a {@code 401} must carry a challenge (RFC 9110, section 15.5.2). The security
 * headers are added to every request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20ClientAuthenticationInterceptor implements HandlerInterceptor {
    /**
     * The challenge for HTTP Basic credentials.
     */
    public static final String BASIC_AUTHENTICATION_CHALLENGE = "Basic realm=\"authentication required\"";

    private final ObjectProvider<List<OAuth20ClientAuthenticationProvider>> clientAuthenticationProviders;

    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<OAuth20RequestParameterResolver> requestParameterResolver;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        OAuth20Utils.addSecurityHeaders(request, response);
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_CLIENT_AUTHENTICATION_REQUEST, Boolean.TRUE);
        for (val provider : getClientAuthenticationProviders()) {
            val credentials = readCredentials(provider, request, response);
            if (credentials == null) {
                continue;
            }
            try {
                val result = provider.getAuthenticationProvider().authenticate(credentials);
                if (result != null && result.getPrincipal() instanceof final OAuth20UserProfile profile) {
                    if (provider.isRequestedClientIgnored() || isClientOfRequest(request, profile)) {
                        LOGGER.debug("Client authentication method [{}] authenticated [{}]", provider.getName(), profile.getId());
                        keepAuthenticatedProfile(request, profile);
                        return true;
                    }
                    LOGGER.warn("Client authentication method [{}] authenticated a client other than the one the request names", provider.getName());
                }
            } catch (final OAuth20ClientAuthenticationResponseException e) {
                LOGGER.debug("Client authentication method [{}] answers the request with status [{}]", provider.getName(), e.getStatus());
                writeResponse(response, e);
                return false;
            } catch (final Exception e) {
                LOGGER.info("Client authentication method [{}] failed: [{}]", provider.getName(), e.getMessage());
                LOGGER.debug(e.getMessage(), e);
            }
        }
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, BASIC_AUTHENTICATION_CHALLENGE);
        response.sendError(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    /**
     * Keep the profile for the request, recording the client of the request as its {@code oauthClientId} attribute.
     *
     * @param request the request
     * @param profile the profile
     */
    protected void keepAuthenticatedProfile(final HttpServletRequest request, final OAuth20UserProfile profile) {
        val clientId = OAuth20Utils.resolveClientIdFromRequest(request, servicesManager.getObject(), requestParameterResolver.getObject());
        if (StringUtils.isNotBlank(clientId)) {
            profile.addAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID, clientId);
        }
        request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE, profile);
    }

    /**
     * Whether the profile was authenticated for the client that the request names, as the token request validators and
     * extractors resolve it: the client of the HTTP Basic credentials, or else the {@code client_id} parameter. A request
     * that names no client, or a profile that records no client, is accepted. A method whose client does not come from the
     * request ({@link OAuth20ClientAuthenticationProvider#isRequestedClientIgnored()}) is not asked.
     *
     * @param request the request
     * @param profile the profile
     * @return true or false
     */
    protected boolean isClientOfRequest(final HttpServletRequest request, final OAuth20UserProfile profile) {
        val authenticatedClientId = CollectionUtils.firstElement(profile.getAttribute(OAuth20Constants.CLIENT_ID)).map(Object::toString);
        if (authenticatedClientId.isEmpty()) {
            return true;
        }
        try {
            val requestedClientId = requestParameterResolver.getObject().resolveClientIdAndClientSecret(request).getLeft();
            return StringUtils.isBlank(requestedClientId) || requestedClientId.equals(authenticatedClientId.get());
        } catch (final AuthenticationException e) {
            LOGGER.debug("Unable to determine the client that the request names: [{}]", e.getMessage());
            return false;
        }
    }

    protected List<OAuth20ClientAuthenticationProvider> getClientAuthenticationProviders() {
        val providers = new ArrayList<>(Objects.requireNonNullElseGet(clientAuthenticationProviders.getIfAvailable(), List::of));
        AnnotationAwareOrderComparator.sort(providers);
        return providers;
    }

    private static @Nullable Authentication readCredentials(final OAuth20ClientAuthenticationProvider provider,
                                                            final HttpServletRequest request,
                                                            final HttpServletResponse response) {
        try {
            val credentials = provider.getAuthenticationConverter().convert(request);
            if (credentials instanceof final OAuth20ClientAuthenticationToken token) {
                token.setResponse(response);
            }
            return credentials;
        } catch (final AuthenticationException e) {
            LOGGER.info("Unable to read the credentials of client authentication method [{}]: [{}]", provider.getName(), e.getMessage());
            return null;
        }
    }

    private static void writeResponse(final HttpServletResponse response,
                                      final OAuth20ClientAuthenticationResponseException exception)
            throws IOException {
        exception.getHeaders().forEach(response::setHeader);
        response.setStatus(exception.getStatus());
        try (val writer = response.getWriter()) {
            writer.write(exception.getBody());
        }
    }
}
