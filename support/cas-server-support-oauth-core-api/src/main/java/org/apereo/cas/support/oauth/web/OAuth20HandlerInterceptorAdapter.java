package org.apereo.cas.support.oauth.web;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is {@link OAuth20HandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OAuth20HandlerInterceptorAdapter extends HandlerInterceptorAdapter {
    /**
     * Access token interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;

    /**
     * Authorization interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor;

    private final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    private final ServicesManager servicesManager;

    private final SessionStore<JEEContext> sessionStore;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        if (requestRequiresAuthentication(request, response)) {
            return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
        }

        if (isDeviceTokenRequest(request, response)) {
            return requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
        }

        return !isAuthorizationRequest(request, response) || requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
    }

    /**
    * Is the client requesting is a OAuth "public" client?
    * An OAuth "public" client is one that does not define a secret like a mobile application.
    *
    * @param request the request
    * @param response the response
    * @return true/false
    */
    protected boolean clientNeedAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        val clientId = OAuth20Utils.getClientIdAndClientSecret(new JEEContext(request, response, sessionStore)).getLeft();
        if (clientId.isEmpty()) {
            return true;
        }

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        if (registeredService == null) {
            return true;
        }
        return OAuth20Utils.doesServiceNeedAuthentication(registeredService);
    }

    /**
     * Is a revoke token request?
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean isRevokeTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, OAuth20Constants.REVOCATION_URL);
    }

    /**
     * Is access token request request.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean isAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        val pattern = String.format("(%s|%s)", OAuth20Constants.ACCESS_TOKEN_URL, OAuth20Constants.TOKEN_URL);
        return doesUriMatchPattern(requestPath, pattern);
    }

    /**
     * Is device token request boolean.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean isDeviceTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        val pattern = String.format("(%s)", OAuth20Constants.DEVICE_AUTHZ_URL);
        return doesUriMatchPattern(requestPath, pattern);
    }

    /**
     * Request requires authentication.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean requestRequiresAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        val revokeTokenRequest = isRevokeTokenRequest(request, response);

        if (revokeTokenRequest) {
            return clientNeedAuthentication(request, response);
        }

        val accessTokenRequest = isAccessTokenRequest(request, response);
        if (!accessTokenRequest) {
            val extractor = extractAccessTokenGrantRequest(request);
            if (extractor.isPresent()) {
                val ext = extractor.get();
                return ext.requestMustBeAuthenticated();
            }
        } else {
            val extractor = extractAccessTokenGrantRequest(request);
            if (extractor.isPresent()) {
                val ext = extractor.get();
                return ext.getResponseType() != OAuth20ResponseTypes.DEVICE_CODE;
            }
        }
        return false;
    }

    private Optional<AccessTokenGrantRequestExtractor> extractAccessTokenGrantRequest(final HttpServletRequest request) {
        return this.accessTokenGrantRequestExtractors
            .stream()
            .filter(ext -> ext.supports(request))
            .findFirst();
    }

    /**
     * Is authorization request.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean isAuthorizationRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, OAuth20Constants.AUTHORIZE_URL);
    }

    /**
     * Does uri match pattern.
     *
     * @param requestPath the request path
     * @param patternUrl  the pattern
     * @return true/false
     */
    protected boolean doesUriMatchPattern(final String requestPath, final String patternUrl) {
        val pattern = Pattern.compile('/' + patternUrl + "(/)*$");
        return pattern.matcher(requestPath).find();
    }
}
