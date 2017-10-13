package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * This is {@link OidcHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcHandlerInterceptorAdapter extends OAuth20HandlerInterceptorAdapter {
    private final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor;
    private final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode;
    private final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors;

    public OidcHandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor,
                                         final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode,
                                         final Collection<BaseAccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor, accessTokenGrantRequestExtractors);
        this.requiresAuthenticationDynamicRegistrationInterceptor = requiresAuthenticationDynamicRegistrationInterceptor;
        this.dynamicClientRegistrationMode = dynamicClientRegistrationMode;
        this.accessTokenGrantRequestExtractors = accessTokenGrantRequestExtractors;
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
