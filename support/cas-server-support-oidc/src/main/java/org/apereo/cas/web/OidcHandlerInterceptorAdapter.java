package org.apereo.cas.web;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcHandlerInterceptorAdapter extends OAuth20HandlerInterceptorAdapter {
    public OidcHandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor);
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (super.preHandle(request, response, handler)) {
            if (isDynamicClientRegistrationRequest(request.getRequestURI())) {
                return requiresAuthenticationAccessTokenInterceptor.preHandle(request, response, handler);
            }
        }
        return false;
    }

    /**
     * Is dynamic client registration request.
     *
     * @param requestPath the request path
     * @return the boolean
     */
    protected boolean isDynamicClientRegistrationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, OidcConstants.REGISTRATION_URL);
    }
}
