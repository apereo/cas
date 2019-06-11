package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
     * Is access token request request.
     *
     * @param request  the request
     * @param response the response
     * @return the boolean
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
     * @return the boolean
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
     * @return the boolean
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
     * @return the boolean
     */
    protected boolean doesUriMatchPattern(final String requestPath, final String patternUrl) {
        val pattern = Pattern.compile('/' + patternUrl + "(/)*$");
        return pattern.matcher(requestPath).find();
    }
}
