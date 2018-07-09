package org.apereo.cas.support.oauth.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * This is {@link OAuth20HandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
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
        if (isAccessTokenRequestRequest(request, response)) {
            return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
        }

        if (isAuthorizationRequest(request, response)) {
            return requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
        }
        return true;
    }

    /**
     * Is access token request request.
     *
     * @param request  the request
     * @param response the response
     * @return the boolean
     */
    protected boolean isAccessTokenRequestRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        val value = doesUriMatchPattern(requestPath, OAuth20Constants.ACCESS_TOKEN_URL)
            || doesUriMatchPattern(requestPath, OAuth20Constants.TOKEN_URL);
        if (!value) {
            val extractor = this.accessTokenGrantRequestExtractors
                .stream()
                .filter(ext -> ext.supports(request))
                .findFirst()
                .orElse(null);
            if (extractor != null) {
                return extractor.requestMustBeAuthenticated();
            }
        }
        return value;
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
