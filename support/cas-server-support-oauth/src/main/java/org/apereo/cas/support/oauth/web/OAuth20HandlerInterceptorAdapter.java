package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
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
public class OAuth20HandlerInterceptorAdapter extends HandlerInterceptorAdapter {
    /**
     * Access token interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;

    /**
     * Authorization interceptor.
     */
    protected final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor;

    private final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    public OAuth20HandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                            final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor,
                                            final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
        this.requiresAuthenticationAccessTokenInterceptor = requiresAuthenticationAccessTokenInterceptor;
        this.requiresAuthenticationAuthorizeInterceptor = requiresAuthenticationAuthorizeInterceptor;
        this.accessTokenGrantRequestExtractors = accessTokenGrantRequestExtractors;
    }

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
        final String requestPath = request.getRequestURI();
        final boolean value = doesUriMatchPattern(requestPath, OAuth20Constants.ACCESS_TOKEN_URL)
                || doesUriMatchPattern(requestPath, OAuth20Constants.TOKEN_URL);
        if (!value) {
            final BaseAccessTokenGrantRequestExtractor extractor = this.accessTokenGrantRequestExtractors
                    .stream()
                    .filter(ext -> ext.supports(request))
                    .findFirst()
                    .orElse(null);
            if (extractor != null) {
                return extractor.getGrantType() == OAuth20GrantTypes.CLIENT_CREDENTIALS
                        || extractor.getGrantType() == OAuth20GrantTypes.PASSWORD;
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
        final String requestPath = request.getRequestURI();
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
