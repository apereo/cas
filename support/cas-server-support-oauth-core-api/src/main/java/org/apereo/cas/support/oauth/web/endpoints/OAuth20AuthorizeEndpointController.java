package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * Handle request via GET.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {

        ensureSessionReplicationIsAutoconfiguredIfNeedBe(request);

        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());

        if (context.getRequestAttribute(OAuth20Constants.ERROR).isPresent()) {
            val mv = getConfigurationContext().getOauthInvalidAuthorizationResponseBuilder().build(context);
            if (!mv.isEmpty() && mv.hasView()) {
                return mv;
            }
        }

        val clientId = OAuth20Utils.getRequestParameter(context, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val registeredService = getRegisteredServiceByClientId(clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);

        if (isRequestAuthenticated(manager, context, registeredService)) {
            val mv = getConfigurationContext().getConsentApprovalViewResolver().resolve(context, registeredService);
            if (!mv.isEmpty() && mv.hasView()) {
                LOGGER.debug("Redirecting to consent-approval view with model [{}]", mv.getModel());
                return mv;
            }
        }

        return redirectToCallbackRedirectUrl(manager, registeredService, context, clientId);
    }

    /**
     * Handle request post.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @PostMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL)
    public ModelAndView handleRequestPost(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return handleRequest(request, response);
    }

    /**
     * Is the request authenticated?
     *
     * @param manager           the Profile Manager
     * @param context           the context
     * @param registeredService the registered service
     * @return whether the request is authenticated or not
     */
    protected boolean isRequestAuthenticated(final ProfileManager manager, final WebContext context,
                                             final OAuthRegisteredService registeredService) {
        val opt = manager.getProfile();
        return opt.isPresent();
    }

    /**
     * Ensure Session Replication Is Auto-Configured If needed.
     *
     * @param request the request
     */
    protected void ensureSessionReplicationIsAutoconfiguredIfNeedBe(final HttpServletRequest request) {
        val casProperties = getConfigurationContext().getCasProperties();
        val replicationRequested = casProperties.getAuthn().getOauth().isReplicateSessions();
        val cookieAutoconfigured = casProperties.getSessionReplication().getCookie().isAutoConfigureCookiePath();
        if (replicationRequested && cookieAutoconfigured) {
            val contextPath = request.getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val path = getConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for OAuth distributed session cookie generator to: [{}]", cookiePath);
                getConfigurationContext().getOauthDistributedSessionCookieGenerator().setCookiePath(cookiePath);
            } else {
                LOGGER.trace("OAuth distributed cookie domain is [{}] with path [{}]",
                    getConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookieDomain(), path);
            }
        }
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Redirect to callback redirect url model and view.
     *
     * @param manager           the manager
     * @param registeredService the registered service
     * @param context           the context
     * @param clientId          the client id
     * @return the model and view
     */
    protected ModelAndView redirectToCallbackRedirectUrl(final ProfileManager manager,
                                                         final OAuthRegisteredService registeredService,
                                                         final JEEContext context,
                                                         final String clientId) {
        val profile = manager.getProfile().orElseThrow(() -> new IllegalArgumentException("Unable to locate authentication profile"));
        val service = getConfigurationContext().getAuthenticationBuilder()
            .buildService(registeredService, context, false);
        LOGGER.trace("Created service [{}] based on registered service [{}]", service, registeredService);

        val authentication = getConfigurationContext().getAuthenticationBuilder()
            .build(profile, registeredService, context, service);
        LOGGER.trace("Created OAuth authentication [{}] for service [{}]", authentication, service);

        try {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            LoggingUtils.error(LOGGER, e);
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val modelAndView = buildAuthorizationForRequest(registeredService, context, clientId, service, authentication);
        if (modelAndView != null && modelAndView.hasView()) {
            return modelAndView;
        }
        LOGGER.trace("No explicit view was defined as part of the authorization response");
        return null;
    }

    /**
     * Build callback url for request string.
     *
     * @param registeredService the registered service
     * @param context           the context
     * @param clientId          the client id
     * @param service           the service
     * @param authentication    the authentication
     * @return the string
     */
    @SneakyThrows
    protected ModelAndView buildAuthorizationForRequest(final OAuthRegisteredService registeredService,
                                                        final JEEContext context,
                                                        final String clientId,
                                                        final Service service,
                                                        final Authentication authentication) {
        val builder = getConfigurationContext().getOauthAuthorizationResponseBuilders().getObject()
            .stream()
            .filter(b -> b.supports(context))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Could not build the callback url. Response type likely not supported"));

        var ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            getConfigurationContext().getTicketGrantingTicketCookieGenerator(),
            getConfigurationContext().getTicketRegistry(), context.getNativeRequest());

        if (ticketGrantingTicket == null) {
            ticketGrantingTicket = getConfigurationContext().getSessionStore()
                .get(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
                .map(ticketId -> getConfigurationContext().getCentralAuthenticationService().getTicket(ticketId.toString(), TicketGrantingTicket.class))
                .orElse(null);
        }
        if (ticketGrantingTicket == null) {
            val message = String.format("Missing ticket-granting-ticket for client id [%s] and service [%s]", clientId, registeredService.getName());
            LOGGER.error(message);
            return OAuth20Utils.produceErrorView(new PreventedException(message));
        }
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf)
            .orElseGet(OAuth20GrantTypes.AUTHORIZATION_CODE::getType)
            .toUpperCase();

        val scopes = OAuth20Utils.parseRequestScopes(context);
        val codeChallenge = context.getRequestParameter(OAuth20Constants.CODE_CHALLENGE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val codeChallengeMethod = context.getRequestParameter(OAuth20Constants.CODE_CHALLENGE_METHOD)
            .map(String::valueOf).orElse(StringUtils.EMPTY)
            .toUpperCase();

        val claims = OAuth20Utils.parseRequestClaims(context);
        val holder = AccessTokenRequestDataHolder.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .ticketGrantingTicket(ticketGrantingTicket)
            .grantType(OAuth20GrantTypes.valueOf(grantType))
            .codeChallenge(codeChallenge)
            .codeChallengeMethod(codeChallengeMethod)
            .scopes(scopes)
            .clientId(clientId)
            .claims(claims)
            .build();

        LOGGER.debug("Building authorization response for grant type [{}] with scopes [{}] for client id [{}]", grantType, scopes, clientId);
        return builder.build(context, clientId, holder);
    }
}
