package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.events.OAuth20AuthorizationRequestEvent;
import org.apereo.cas.support.oauth.events.OAuth20AuthorizationResponseEvent;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.util.JsonUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.core.OrderComparator;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * When the request is valid, this endpoint is protected by a CAS authentication.
 * It returns an OAuth code or directly an access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class OAuth20AuthorizeEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    public OAuth20AuthorizeEndpointController(final T oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    private static Optional<OAuth20AuthorizationRequest.OAuth20AuthorizationRequestBuilder> toAuthorizationRequest(
        final OAuthRegisteredService registeredService,
        final JEEContext context, final Service service,
        final Authentication authentication,
        final OAuth20AuthorizationResponseBuilder builder) {
        return builder.toAuthorizationRequest(context, authentication, service, registeredService);
    }

    /**
     * Handle request via GET.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Throwable the throwable
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) throws Throwable {
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();

        val webContext = new JEEContext(request, response);
        val prompts = requestParameterResolver.resolveSupportedPromptValues(webContext);
        val requestedPrompt = requestParameterResolver.resolveRequestedPromptValues(webContext);
        if (!requestedPrompt.isEmpty() && !requestedPrompt.equals(prompts)) {
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST, "Unsupported prompt parameter value");
        }
        ensureSessionReplicationIsAutoconfiguredIfNeedBe(request);

        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());

        if (context.getRequestAttribute(OAuth20Constants.ERROR).isPresent()) {
            val mv = getConfigurationContext().getOauthInvalidAuthorizationResponseBuilder().build(context);
            if (!mv.isEmpty() && mv.hasView()) {
                return mv;
            }
        }

        val clientId = requestParameterResolver
            .resolveRequestParameter(context, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val registeredService = getRegisteredServiceByClientId(clientId);
        val clientService = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientService, registeredService);

        if (LoggingUtils.isProtocolMessageLoggerEnabled()) {
            val redirectUri = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI).orElse(StringUtils.EMPTY);
            val responseType = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE).orElse(StringUtils.EMPTY);
            val scopes = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.SCOPE).orElse(StringUtils.EMPTY);
            val state = requestParameterResolver.resolveRequestParameter(context, OAuth20Constants.STATE).orElse(StringUtils.EMPTY);
            val protocolContext = Map.of("Registered Service", registeredService.getName(), "Client ID", clientId, "State", state,
                "Redirect URI", redirectUri, "Response Type", responseType, "Scopes", scopes);
            LoggingUtils.protocolMessage("OAuth/OpenID Connect Authorization Request", protocolContext);
            configurationContext.getApplicationContext().publishEvent(
                new OAuth20AuthorizationRequestEvent(this, ClientInfoHolder.getClientInfo(), protocolContext));
        }

        if (isRequestAuthenticated(manager, context, registeredService)) {
            val mv = getConfigurationContext().getConsentApprovalViewResolver().resolve(context, registeredService);
            if (!mv.isEmpty() && mv.hasView()) {
                LOGGER.debug("Redirecting to consent-approval view with model [{}]", mv.getModel());
                return mv;
            }
        }

        return redirectToCallbackRedirectUrl(manager, registeredService, context);
    }

    /**
     * Handle request post.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Throwable the throwable
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return handleRequest(request, response);
    }


    protected ModelAndView redirectToCallbackRedirectUrl(final ProfileManager manager,
                                                         final OAuthRegisteredService registeredService,
                                                         final JEEContext context) throws Throwable {
        val profile = verifyAndReturnAuthenticatedProfile(manager, context);
        val service = getConfigurationContext().getAuthenticationBuilder()
            .buildService(registeredService, context, false);
        LOGGER.trace("Created service [{}] based on registered service [{}]", service, registeredService);

        val authentication = getConfigurationContext().getAuthenticationBuilder()
            .build(profile, registeredService, context, service);
        LOGGER.trace("Created OAuth authentication [{}] for service [{}]", authentication, service);

        try {
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .principal(authentication.getPrincipal())
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            val modelAndView = buildAuthorizationForRequest(registeredService, context, service, authentication);
            return Optional.ofNullable(modelAndView)
                .filter(ModelAndView::hasView)
                .orElseGet(() -> {
                    LOGGER.trace("No explicit view was defined as part of the authorization response");
                    return null;
                });
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
            return OAuth20Utils.produceUnauthorizedErrorView(HttpStatus.FORBIDDEN);
        }
    }

    private UserProfile verifyAndReturnAuthenticatedProfile(final ProfileManager manager, final JEEContext context) {
        val casProperties = getConfigurationContext().getCasProperties();
        return manager.getProfile()
            .orElseThrow(() -> new IllegalArgumentException("""
                CAS is unable to locate authentication profile for this request: %s. The authentication request is expected
                to have been verified with an authentication attempt, and yet there is no record of an authentication event.
                This issue is typically the result of CAS misconfiguration. Please examine your CAS setup and review your
                OIDC configuration. Your OIDC issuer is defined as %s, and your CAS server name is %s.
                """
                .stripIndent()
                .formatted(
                    context.getFullRequestURL(),
                    casProperties.getAuthn().getOidc().getCore().getIssuer(),
                    casProperties.getServer().getPrefix())));
    }

    protected ModelAndView buildAuthorizationForRequest(
        final OAuthRegisteredService registeredService,
        final JEEContext context, final Service service,
        final Authentication authentication) {

        val registeredBuilders = getConfigurationContext().getOauthAuthorizationResponseBuilders().getObject();

        val authzRequest = registeredBuilders
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(OrderComparator.INSTANCE)
            .map(builder -> toAuthorizationRequest(registeredService, context, service, authentication, builder))
            .filter(Objects::nonNull)
            .filter(Optional::isPresent)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to build authorization request"))
            .orElseThrow()
            .build();

        val payload = Optional.ofNullable(authzRequest.getAccessTokenRequest())
            .orElseGet(Unchecked.supplier(() -> prepareAccessTokenRequestContext(authzRequest,
                registeredService, context, service, authentication)));

        val result = registeredBuilders
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(OrderComparator.INSTANCE)
            .filter(builder -> builder.supports(authzRequest))
            .findFirst()
            .map(Unchecked.function(builder -> {
                if (authzRequest.isSingleSignOnSessionRequired() && payload.getTicketGrantingTicket() == null
                    && !OAuth20Utils.isStatelessAuthentication(payload.getUserProfile())) {
                    val message = String.format("Missing ticket-granting-ticket for client id [%s] and service [%s]",
                        authzRequest.getClientId(), registeredService.getName());
                    LOGGER.error(message);
                    return OAuth20Utils.produceErrorView(new PreventedException(message));
                }
                return builder.build(payload);
            }))
            .orElseGet(() -> OAuth20Utils.produceErrorView(new PreventedException("Could not build the callback response")));

        if (LoggingUtils.isProtocolMessageLoggerEnabled()) {
            val protocolContext = Map.<String, Object>of("Service", service.getId(), "Client ID", payload.getClientId(),
                "Response Mode", payload.getResponseMode(), "Response Type", payload.getResponseType(),
                "Redirect URI", payload.getRedirectUri());
            LoggingUtils.protocolMessage("OAuth/OpenID Connect Authorization Response",
                protocolContext,
                result.getModel().isEmpty() ? StringUtils.EMPTY : JsonUtils.render(result.getModel()));
            configurationContext.getApplicationContext().publishEvent(
                new OAuth20AuthorizationResponseEvent(this, ClientInfoHolder.getClientInfo(), protocolContext));
        }
        return result;
    }

    protected AccessTokenRequestContext prepareAccessTokenRequestContext(
        final OAuth20AuthorizationRequest authzRequest,
        final OAuthRegisteredService registeredService,
        final JEEContext context,
        final Service service,
        final Authentication authentication) throws Exception {

        var payloadBuilder = AccessTokenRequestContext.builder();
        if (authzRequest.isSingleSignOnSessionRequired()) {
            val tgt = getConfigurationContext().fetchTicketGrantingTicketFrom(context);
            payloadBuilder = payloadBuilder.ticketGrantingTicket(tgt);
        }
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        val redirectUri = requestParameterResolver
            .resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf)
            .orElseGet(OAuth20GrantTypes.AUTHORIZATION_CODE::getType)
            .toUpperCase(Locale.ENGLISH);
        val scopes = requestParameterResolver.resolveRequestScopes(context);
        val codeChallenge = context.getRequestParameter(OAuth20Constants.CODE_CHALLENGE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        val challengeMethodsSupported = getConfigurationContext().getCasProperties().getAuthn().getOidc().getDiscovery().getCodeChallengeMethodsSupported();
        val codeChallengeMethod = context.getRequestParameter(OAuth20Constants.CODE_CHALLENGE_METHOD)
            .map(String::valueOf)
            .filter(challengeMethodsSupported::contains)
            .orElse(StringUtils.EMPTY)
            .toUpperCase(Locale.ENGLISH);

        val userProfile = OAuth20Utils.getAuthenticatedUserProfile(context, getConfigurationContext().getSessionStore());
        val claims = requestParameterResolver.resolveRequestClaims(context);
        val holder = payloadBuilder
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(requestParameterResolver.resolveGrantType(context))
            .responseType(requestParameterResolver.resolveResponseType(context))
            .codeChallenge(codeChallenge)
            .codeChallengeMethod(codeChallengeMethod)
            .scopes(scopes)
            .clientId(authzRequest.getClientId())
            .redirectUri(redirectUri)
            .userProfile(userProfile)
            .claims(claims)
            .responseMode(requestParameterResolver.resolveResponseModeType(context))
            .build();
        context.getRequestParameters().keySet()
            .forEach(key -> context.getRequestParameter(key).ifPresent(value -> holder.getParameters().put(key, value)));
        LOGGER.debug("Building authorization response for grant type [{}] with scopes [{}] for client id [{}]",
            grantType, scopes, authzRequest.getClientId());
        return holder;
    }
    
}
