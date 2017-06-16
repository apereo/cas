package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

/**
 * This is {@link OAuth20HandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OAuth20HandlerInterceptorAdapter extends HandlerInterceptorAdapter {
    /** Access token interceptor. */
    protected final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;

    /** Authorization interceptor. */
    protected final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor;

    public OAuth20HandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                            final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor) {
        this.requiresAuthenticationAccessTokenInterceptor = requiresAuthenticationAccessTokenInterceptor;
        this.requiresAuthenticationAuthorizeInterceptor = requiresAuthenticationAuthorizeInterceptor;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        if (isAccessTokenRequestRequest(request.getRequestURI())) {
            return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
        }

        if (isAuthorizationRequest(request.getRequestURI())) {
            return requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
        }
        return true;
    }

    /**
     * Is access token request request.
     *
     * @param requestPath the request path
     * @return the boolean
     */
    protected boolean isAccessTokenRequestRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, OAuth20Constants.ACCESS_TOKEN_URL)
                || doesUriMatchPattern(requestPath, OAuth20Constants.TOKEN_URL);
    }

    /**
     * Is authorization request.
     *
     * @param requestPath the request path
     * @return the boolean
     */
    protected boolean isAuthorizationRequest(final String requestPath) {
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
        final Pattern pattern = Pattern.compile('/' + patternUrl + "(/)*$");
        return pattern.matcher(requestPath).find();
    }
}
