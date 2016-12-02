package org.apereo.cas.support.oauth.web;

import org.apereo.cas.support.oauth.OAuthConstants;
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
    private final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor;
    private final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor;

    public OAuth20HandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                            final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor) {
        this.requiresAuthenticationAccessTokenInterceptor = requiresAuthenticationAccessTokenInterceptor;
        this.requiresAuthenticationAuthorizeInterceptor = requiresAuthenticationAuthorizeInterceptor;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        final String requestPath = request.getRequestURI();
        Pattern pattern = Pattern.compile('/' + OAuthConstants.ACCESS_TOKEN_URL + "(/)*$");

        if (pattern.matcher(requestPath).find()) {
            return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
        }

        pattern = Pattern.compile('/' + OAuthConstants.AUTHORIZE_URL + "(/)*$");
        if (pattern.matcher(requestPath).find()) {
            return requiresAuthenticationAuthorizeInterceptor.preHandle(request, response, handler);
        }
        return true;
    }
}
