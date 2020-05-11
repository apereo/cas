package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;

import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
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
@Slf4j
public class OidcHandlerInterceptorAdapter extends OAuth20HandlerInterceptorAdapter {
    private final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor;

    private final HandlerInterceptorAdapter requiresAuthenticationClientConfigurationInterceptor;

    private final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode;

    public OidcHandlerInterceptorAdapter(final HandlerInterceptorAdapter requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationAuthorizeInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationDynamicRegistrationInterceptor,
                                         final HandlerInterceptorAdapter requiresAuthenticationClientConfigurationInterceptor,
                                         final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode,
                                         final Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors,
                                         final ServicesManager servicesManager,
                                         final SessionStore<JEEContext> sessionStore) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor, accessTokenGrantRequestExtractors, servicesManager, sessionStore);
        this.requiresAuthenticationDynamicRegistrationInterceptor = requiresAuthenticationDynamicRegistrationInterceptor;
        this.dynamicClientRegistrationMode = dynamicClientRegistrationMode;
        this.requiresAuthenticationClientConfigurationInterceptor = requiresAuthenticationClientConfigurationInterceptor;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        LOGGER.trace("Attempting to pre-handle OIDC request at [{}]", request.getRequestURI());
        if (!super.preHandle(request, response, handler)) {
            LOGGER.trace("Unable to pre-handle OIDC request at [{}]", request.getRequestURI());
            return false;
        }

        if (isClientConfigurationRequest(request.getRequestURI())) {
            LOGGER.trace("OIDC client configuration is protected at [{}]", request.getRequestURI());
            return requiresAuthenticationClientConfigurationInterceptor.preHandle(request, response, handler);

        }
        if (isDynamicClientRegistrationRequest(request.getRequestURI())) {
            LOGGER.trace("OIDC request at [{}] is one of dynamic client registration", request.getRequestURI());
            if (isDynamicClientRegistrationRequestProtected()) {
                LOGGER.trace("OIDC dynamic client registration is protected at [{}]", request.getRequestURI());
                return requiresAuthenticationDynamicRegistrationInterceptor.preHandle(request, response, handler);
            }
        }
        return true;
    }

    /**
     * Is dynamic client registration request protected boolean.
     *
     * @return true/false
     */
    private boolean isDynamicClientRegistrationRequestProtected() {
        return this.dynamicClientRegistrationMode == OidcConstants.DynamicClientRegistrationMode.PROTECTED;
    }

    /**
     * Is dynamic client registration request.
     *
     * @param requestPath the request path
     * @return true/false
     */
    protected boolean isDynamicClientRegistrationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, OidcConstants.REGISTRATION_URL);
    }

    /**
     * Is client configuration request.
     *
     * @param requestPath the request path
     * @return true/false
     */
    protected boolean isClientConfigurationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, OidcConstants.CLIENT_CONFIGURATION_URL);
    }
}
