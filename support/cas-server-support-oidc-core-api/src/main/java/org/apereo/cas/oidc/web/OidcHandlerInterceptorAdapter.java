package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link OidcHandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class OidcHandlerInterceptorAdapter extends OAuth20HandlerInterceptorAdapter {
    private final HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor;

    private final HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor;

    private final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode;

    public OidcHandlerInterceptorAdapter(final HandlerInterceptor requiresAuthenticationAccessTokenInterceptor,
                                         final HandlerInterceptor requiresAuthenticationAuthorizeInterceptor,
                                         final HandlerInterceptor requiresAuthenticationDynamicRegistrationInterceptor,
                                         final HandlerInterceptor requiresAuthenticationClientConfigurationInterceptor,
                                         final OidcConstants.DynamicClientRegistrationMode dynamicClientRegistrationMode,
                                         final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
                                         final ServicesManager servicesManager,
                                         final SessionStore sessionStore,
                                         final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor,
            accessTokenGrantRequestExtractors, servicesManager, sessionStore, oauthAuthorizationRequestValidators);
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
     * Is dynamic client registration request.
     *
     * @param requestPath the request path
     * @return true/false
     */
    protected boolean isDynamicClientRegistrationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, CollectionUtils.wrapList(OidcConstants.REGISTRATION_URL));
    }

    /**
     * Is client configuration request.
     *
     * @param requestPath the request path
     * @return true/false
     */
    protected boolean isClientConfigurationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, CollectionUtils.wrapList(OidcConstants.CLIENT_CONFIGURATION_URL));
    }

    @Override
    protected List<String> getRevocationUrls() {
        val urls = super.getRevocationUrls();
        urls.add(OidcConstants.REVOCATION_URL);
        return urls;
    }

    @Override
    protected List<String> getAccessTokenUrls() {
        val accessTokenUrls = super.getAccessTokenUrls();
        accessTokenUrls.add(OidcConstants.ACCESS_TOKEN_URL);
        accessTokenUrls.add(OidcConstants.TOKEN_URL);
        return accessTokenUrls;
    }

    @Override
    protected List<String> getAuthorizeUrls() {
        val urls = super.getAuthorizeUrls();
        urls.add(OidcConstants.AUTHORIZE_URL);
        return urls;
    }

    /**
     * Is dynamic client registration request protected boolean.
     *
     * @return true/false
     */
    private boolean isDynamicClientRegistrationRequestProtected() {
        return this.dynamicClientRegistrationMode == OidcConstants.DynamicClientRegistrationMode.PROTECTED;
    }
}
