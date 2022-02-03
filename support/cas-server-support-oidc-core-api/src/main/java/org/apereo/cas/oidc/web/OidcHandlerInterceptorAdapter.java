package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20HandlerInterceptorAdapter;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
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
    private final ObjectProvider<HandlerInterceptor> requiresAuthenticationDynamicRegistrationInterceptor;

    private final ObjectProvider<HandlerInterceptor> requiresAuthenticationClientConfigurationInterceptor;

    private final CasConfigurationProperties casProperties;

    public OidcHandlerInterceptorAdapter(
        final ObjectProvider<HandlerInterceptor> requiresAuthenticationAccessTokenInterceptor,
        final ObjectProvider<HandlerInterceptor> requiresAuthenticationAuthorizeInterceptor,
        final ObjectProvider<HandlerInterceptor> requiresAuthenticationDynamicRegistrationInterceptor,
        final ObjectProvider<HandlerInterceptor> requiresAuthenticationClientConfigurationInterceptor,
        final CasConfigurationProperties casProperties,
        final ObjectProvider<List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<SessionStore> sessionStore,
        final ObjectProvider<List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators) {
        super(requiresAuthenticationAccessTokenInterceptor, requiresAuthenticationAuthorizeInterceptor,
            accessTokenGrantRequestExtractors, servicesManager, sessionStore, oauthAuthorizationRequestValidators);

        this.requiresAuthenticationDynamicRegistrationInterceptor = requiresAuthenticationDynamicRegistrationInterceptor;
        this.casProperties = casProperties;
        this.requiresAuthenticationClientConfigurationInterceptor = requiresAuthenticationClientConfigurationInterceptor;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws Exception {
        LOGGER.trace("Attempting to pre-handle OIDC request at [{}]", request.getRequestURI());
        if (casProperties.getAuthn().getOidc().getDiscovery().isRequirePushedAuthorizationRequests()
            && HttpMethod.valueOf(request.getMethod()) != HttpMethod.POST
            && StringUtils.isBlank(request.getParameter(OidcConstants.REQUEST_URI))
            && isAuthorizationRequest(request, response)) {
            LOGGER.warn("CAS is configured to only accept pushed authorization requests and this is not a POST");
            response.setStatus(HttpStatus.SC_FORBIDDEN);
            return false;
        }

        if (isPushedAuthorizationRequest(request.getRequestURI())) {
            LOGGER.trace("OIDC pushed authorization request is protected at [{}]", request.getRequestURI());
            return requiresAuthenticationAccessTokenInterceptor.getObject().preHandle(request, response, handler);
        }
        
        if (!super.preHandle(request, response, handler)) {
            LOGGER.trace("Unable to pre-handle OIDC request at [{}]", request.getRequestURI());
            return false;
        }

        if (isClientConfigurationRequest(request.getRequestURI())) {
            LOGGER.trace("OIDC client configuration is protected at [{}]", request.getRequestURI());
            return requiresAuthenticationClientConfigurationInterceptor.getObject().preHandle(request, response, handler);

        }
        if (isDynamicClientRegistrationRequest(request.getRequestURI())) {
            LOGGER.trace("OIDC request at [{}] is one of dynamic client registration", request.getRequestURI());
            if (isDynamicClientRegistrationRequestProtected()) {
                LOGGER.trace("OIDC dynamic client registration is protected at [{}]", request.getRequestURI());
                return requiresAuthenticationDynamicRegistrationInterceptor.getObject().preHandle(request, response, handler);
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

    /**
     * Is PAR request.
     *
     * @param requestPath the request path
     * @return true/false
     */
    protected boolean isPushedAuthorizationRequest(final String requestPath) {
        return doesUriMatchPattern(requestPath, CollectionUtils.wrapList(OidcConstants.PUSHED_AUTHORIZE_URL));
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
     * Is dynamic client registration request protected?
     *
     * @return true/false
     */
    private boolean isDynamicClientRegistrationRequestProtected() {
        val oidc = casProperties.getAuthn().getOidc();
        return OidcConstants.DynamicClientRegistrationMode.valueOf(StringUtils.defaultIfBlank(
            oidc.getCore().getDynamicClientRegistrationMode(),
            OidcConstants.DynamicClientRegistrationMode.PROTECTED.name())).isProtected();
    }
}
