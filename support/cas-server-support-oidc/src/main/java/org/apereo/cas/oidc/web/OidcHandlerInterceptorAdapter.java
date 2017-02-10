package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
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
    private final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor;
    private OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode;

    public OidcHandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor,
                                         final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor);
        this.requiresAuthenticationDynamicRegistrationInterceptor = requiresAuthenticationDynamicRegistrationInterceptor;
        this.dynamicClientRegistrationMode = dynamicClientRegistrationMode;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        if (!super.preHandle(request, response, handler)) {
            return false;
        }

        if (isDynamicClientRegistrationRequest(request.getRequestURI())) {
            if (isDynamicClientRegistrationRequestProtected()) {
                return requiresAuthenticationDynamicRegistrationInterceptor.preHandle(request, response, handler);
            }
        }
        return true;
    }

    private boolean isDynamicClientRegistrationRequestProtected() {
        return this.dynamicClientRegistrationMode == OidcConstants.DynamicClientRegistrationMode.PROTECTED;
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
