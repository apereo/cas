package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * This is {@link OAuth20HandlerInterceptorAdapter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OAuth20HandlerInterceptorAdapter implements AsyncHandlerInterceptor {
    /**
     * Access token interceptor.
     */
    protected final ObjectProvider<@NonNull HandlerInterceptor> requiresAuthenticationAccessTokenInterceptor;

    /**
     * Authorization interceptor.
     */
    protected final ObjectProvider<@NonNull HandlerInterceptor> requiresAuthenticationAuthorizeInterceptor;

    private final ObjectProvider<@NonNull List<AccessTokenGrantRequestExtractor>> accessTokenGrantRequestExtractors;

    private final ObjectProvider<@NonNull ServicesManager> servicesManager;

    private final ObjectProvider<@NonNull SessionStore> sessionStore;

    private final ObjectProvider<@NonNull List<OAuth20AuthorizationRequestValidator>> oauthAuthorizationRequestValidators;

    private final ObjectProvider<@NonNull OAuth20RequestParameterResolver> requestParameterResolver;

    @Override
    public boolean preHandle(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler) throws Exception {
        if (requestRequiresAuthentication(request, response)) {
            return requiresAuthenticationAccessTokenInterceptor.getObject().preHandle(request, response, handler);
        }

        if (isDeviceTokenRequest(request, response)) {
            return requiresAuthenticationAuthorizeInterceptor.getObject().preHandle(request, response, handler);
        }

        return !isAuthorizationRequest(request, response) || requiresAuthenticationAuthorizeInterceptor.getObject().preHandle(request, response, handler);
    }

    /**
     * Is the client requesting is a OAuth "public" client?
     * An OAuth "public" client is one that does not define a secret like a mobile application.
     *
     * @param request  the request
     * @param response the response
     * @return true/false
     */
    protected boolean clientNeedAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        val callContext = new CallContext(new JEEContext(request, response), sessionStore.getObject());
        val clientId = requestParameterResolver.getObject().resolveClientIdAndClientSecret(callContext).getLeft();
        if (clientId.isEmpty()) {
            return true;
        }

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager.getObject(), clientId);
        return registeredService == null || OAuth20Utils.doesServiceNeedAuthentication(registeredService);
    }
    
    protected boolean isRevokeTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, getRevocationUrls());
    }

    protected List<String> getRevocationUrls() {
        return CollectionUtils.wrapList(OAuth20Constants.REVOCATION_URL);
    }

    protected boolean isAccessTokenRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, getAccessTokenUrls());
    }

    protected List<String> getAccessTokenUrls() {
        return CollectionUtils.wrapList(OAuth20Constants.ACCESS_TOKEN_URL, OAuth20Constants.TOKEN_URL);
    }

    protected boolean isDeviceTokenRequest(final HttpServletRequest request,
                                           final HttpServletResponse response) {
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, CollectionUtils.wrapList(OAuth20Constants.DEVICE_AUTHZ_URL));
    }
    
    protected boolean requestRequiresAuthentication(final HttpServletRequest request,
                                                    final HttpServletResponse response) {
        val context = new JEEContext(request, response);
        val revokeTokenRequest = isRevokeTokenRequest(request, response);

        if (revokeTokenRequest) {
            return clientNeedAuthentication(request, response);
        }

        val accessTokenRequest = isAccessTokenRequest(request, response);
        val extractor = extractAccessTokenGrantRequest(context);
        if (accessTokenRequest) {
            request.setAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_ACCESS_TOKEN_REQUEST, Boolean.TRUE);
            if (extractor.isPresent()) {
                val ext = extractor.get();
                return ext.getResponseType() != OAuth20ResponseTypes.DEVICE_CODE
                    && ext.getGrantType() != OAuth20GrantTypes.JWT_BEARER;
            }
            return true;
        }
        
        if (extractor.isPresent()) {
            val ext = extractor.get();
            return ext.requestMustBeAuthenticated();
        }
        return false;
    }

    protected boolean isAuthorizationRequest(final HttpServletRequest request,
                                             final HttpServletResponse response) {
        val context = new JEEContext(request, response);
        val requestPath = request.getRequestURI();
        return doesUriMatchPattern(requestPath, getAuthorizeUrls()) && isValidAuthorizeRequest(context);
    }

    protected List<String> getAuthorizeUrls() {
        return CollectionUtils.wrapList(OAuth20Constants.AUTHORIZE_URL);
    }

    /**
     * Does uri match pattern.
     *
     * @param requestPath the request path
     * @param patternUrls the pattern urls
     * @return true /false
     */
    protected boolean doesUriMatchPattern(final String requestPath, final List<String> patternUrls) {
        return patternUrls.stream().anyMatch(patternUrl -> {
            val pattern = Pattern.compile('/' + patternUrl + "(/)*$");
            return pattern.matcher(requestPath).find();
        });
    }

    protected boolean isValidAuthorizeRequest(final JEEContext context) {
        val validator = oauthAuthorizationRequestValidators.getObject()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(Unchecked.predicate(b -> b.supports(context)))
            .findFirst()
            .orElse(null);
        return FunctionUtils.doUnchecked(() -> validator != null && validator.validate(context));
    }

    private Optional<AccessTokenGrantRequestExtractor> extractAccessTokenGrantRequest(
        final WebContext context) {
        return accessTokenGrantRequestExtractors.getObject()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(ext -> ext.supports(context))
            .findFirst();
    }
}
