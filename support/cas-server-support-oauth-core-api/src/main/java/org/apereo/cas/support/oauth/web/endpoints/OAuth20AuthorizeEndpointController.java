package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller is in charge of responding to the authorize call in OAuth v2 protocol.
 * This url is protected by a CAS authentication. It returns an OAuth code or directly an access token.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class OAuth20AuthorizeEndpointController extends BaseOAuth20Controller {
    public OAuth20AuthorizeEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    private static boolean isRequestAuthenticated(final ProfileManager manager) {
        val opt = manager.get(true);
        return opt.isPresent();
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

        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val manager = new ProfileManager<CommonProfile>(context, context.getSessionStore());

        if (!verifyAuthorizeRequest(context) || !isRequestAuthenticated(manager)) {
            LOGGER.error("Authorize request verification failed. Authorization request is missing required parameters, "
                + "or the request is not authenticated and contains no authenticated profile/principal.");
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID)
            .map(String::valueOf)
            .orElse(StringUtils.EMPTY);
        val registeredService = getRegisteredServiceByClientId(clientId);
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(clientId, registeredService);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val mv = getOAuthConfigurationContext().getConsentApprovalViewResolver().resolve(context, registeredService);
        if (!mv.isEmpty() && mv.hasView()) {
            LOGGER.debug("Redirecting to consent-approval view with model [{}]", mv.getModel());
            return mv;
        }

        return redirectToCallbackRedirectUrl(manager, registeredService, context, clientId);
    }

    protected void ensureSessionReplicationIsAutoconfiguredIfNeedBe(final HttpServletRequest request) {
        val casProperties = getOAuthConfigurationContext().getCasProperties();
        val replicationRequested = casProperties.getAuthn().getOauth().isReplicateSessions();
        val cookieAutoconfigured = casProperties.getSessionReplication().getCookie().isAutoConfigureCookiePath();
        if (replicationRequested && cookieAutoconfigured) {
            val contextPath = request.getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val path = getOAuthConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for OAuth distributed session cookie generator to: [{}]", cookiePath);
                getOAuthConfigurationContext().getOauthDistributedSessionCookieGenerator().setCookiePath(cookiePath);
            } else {
                LOGGER.trace("OAuth distributed cookie domain is [{}] with path [{}]",
                        getOAuthConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookieDomain(), path);
            }
        }
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
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
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
    protected ModelAndView redirectToCallbackRedirectUrl(final ProfileManager<CommonProfile> manager,
                                                         final OAuthRegisteredService registeredService,
                                                         final JEEContext context,
                                                         final String clientId) {
        val profileResult = manager.get(true);
        if (profileResult.isEmpty()) {
            LOGGER.error("Unexpected null profile from profile manager. Request is not fully authenticated.");
            return OAuth20Utils.produceUnauthorizedErrorView();
        }
        val profile = profileResult.get();
        val service = getOAuthConfigurationContext().getAuthenticationBuilder()
            .buildService(registeredService, context, false);
        LOGGER.trace("Created service [{}] based on registered service [{}]", service, registeredService);

        val authentication = getOAuthConfigurationContext().getAuthenticationBuilder()
            .build(profile, registeredService, context, service);
        LOGGER.trace("Created OAuth authentication [{}] for service [{}]", service, authentication);

        try {
            val audit = AuditableContext.builder()
                .service(service)
                .authentication(authentication)
                .registeredService(registeredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                .build();
            val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();
        } catch (final UnauthorizedServiceException | PrincipalException e) {
            LOGGER.error(e.getMessage(), e);
            return OAuth20Utils.produceUnauthorizedErrorView();
        }

        val modelAndView = buildAuthorizationForRequest(registeredService, context, clientId, service, authentication);
        if (modelAndView != null && modelAndView.hasView()) {
            return modelAndView;
        }
        LOGGER.debug("No explicit view was defined as part of the authorization response");
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
        val builder = getOAuthConfigurationContext().getOauthAuthorizationResponseBuilders()
            .stream()
            .filter(b -> b.supports(context))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Could not build the callback url. Response type likely not supported"));

        val ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            getOAuthConfigurationContext().getTicketGrantingTicketCookieGenerator(),
            getOAuthConfigurationContext().getTicketRegistry(), context.getNativeRequest());

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

    /**
     * Verify the authorize request.
     *
     * @param context the context
     * @return whether the authorize request is valid
     */
    private boolean verifyAuthorizeRequest(final JEEContext context) {
        val validator = getOAuthConfigurationContext().getOauthRequestValidators()
            .stream()
            .filter(b -> b.supports(context))
            .findFirst()
            .orElse(null);
        if (validator == null) {
            LOGGER.warn("Ignoring malformed request [{}] as no OAuth20 validator could declare support for its syntax", context.getFullRequestURL());
            return false;
        }
        return validator.validate(context);
    }
}


