package org.apereo.cas.web.support;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Common utilities for the web tier.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class WebUtils {

    /**
     * Request attribute that contains message key describing details of authorization failure.
     */
    public static final String CAS_ACCESS_DENIED_REASON = "CAS_ACCESS_DENIED_REASON";

    /**
     * Constant representing the request header for user agent.
     */
    public static final String USER_AGENT_HEADER = "user-agent";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);

    private static final String PUBLIC_WORKSTATION_ATTRIBUTE = "publicWorkstation";
    private static final String PARAMETER_AUTHENTICATION = "authentication";
    private static final String PARAMETER_AUTHENTICATION_RESULT_BUILDER = "authenticationResultBuilder";
    private static final String PARAMETER_AUTHENTICATION_RESULT = "authenticationResult";
    private static final String PARAMETER_CREDENTIAL = "credential";
    private static final String PARAMETER_UNAUTHORIZED_REDIRECT_URL = "unauthorizedRedirectUrl";
    private static final String PARAMETER_TICKET_GRANTING_TICKET_ID = "ticketGrantingTicketId";
    private static final String PARAMETER_REGISTERED_SERVICE = "registeredService";
    private static final String PARAMETER_SERVICE = "service";
    private static final String PARAMETER_SERVICE_TICKET_ID = "serviceTicketId";
    private static final String PARAMETER_LOGOUT_REQUESTS = "logoutRequests";
    private static final String PARAMETER_SERVICE_UI_METADATA = "serviceUIMetadata";

    /**
     * Instantiates a new web utils instance.
     */
    private WebUtils() {
    }

    /**
     * Gets the http servlet request from the context.
     *
     * @param context the context
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequest(final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context.getExternalContext(),
                "Cannot obtain HttpServletRequest from event of type: "
                        + context.getExternalContext().getClass().getName());

        return (HttpServletRequest) context.getExternalContext().getNativeRequest();
    }

    /**
     * Gets the http servlet request from the current servlet context.
     *
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequest() {
        final ServletExternalContext servletExternalContext = (ServletExternalContext) ExternalContextHolder.getExternalContext();
        if (servletExternalContext != null) {
            return (HttpServletRequest) servletExternalContext.getNativeRequest();
        }
        return null;

    }

    /**
     * Gets http servlet request from request attributes.
     *
     * @return the http servlet request from request attributes
     */
    public static HttpServletRequest getHttpServletRequestFromRequestAttributes() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets http servlet response from request attributes.
     *
     * @return the http servlet response from request attributes
     */
    public static HttpServletResponse getHttpServletResponseFromRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    /**
     * Gets the http servlet response from the context.
     *
     * @param context the context
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponse(final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context.getExternalContext(),
                "Cannot obtain HttpServletResponse from event of type: " + context.getExternalContext().getClass().getName());
        return (HttpServletResponse) context.getExternalContext().getNativeResponse();
    }

    /**
     * Gets the http servlet response from the current servlet context.
     *
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponse() {
        final ServletExternalContext servletExternalContext = (ServletExternalContext) ExternalContextHolder.getExternalContext();
        if (servletExternalContext != null) {
            return (HttpServletResponse) servletExternalContext.getNativeResponse();
        }
        return null;
    }

    /**
     * Gets the service from the request based on given extractors.
     *
     * @param argumentExtractors the argument extractors
     * @param request            the request
     * @return the service, or null.
     */
    public static WebApplicationService getService(final List<ArgumentExtractor> argumentExtractors, final HttpServletRequest request) {
        return argumentExtractors.stream().map(argumentExtractor -> argumentExtractor.extractService(request))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Gets the service.
     *
     * @param argumentExtractors the argument extractors
     * @param context            the context
     * @return the service
     */
    public static WebApplicationService getService(final List<ArgumentExtractor> argumentExtractors, final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        return getService(argumentExtractors, request);
    }

    /**
     * Gets the service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static WebApplicationService getService(final RequestContext context) {
        return context != null ? (WebApplicationService) context.getFlowScope().get(PARAMETER_SERVICE) : null;
    }

    /**
     * Gets the registered service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static RegisteredService getRegisteredService(final RequestContext context) {
        return context != null ? (RegisteredService) context.getFlowScope().get(PARAMETER_REGISTERED_SERVICE) : null;
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticket  the ticket value
     */
    public static void putTicketGrantingTicketInScopes(
            final RequestContext context, final TicketGrantingTicket ticket) {
        final String ticketValue = ticket != null ? ticket.getId() : null;
        putTicketGrantingTicketInScopes(context, ticketValue);
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context     the context
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketInScopes(final RequestContext context, final String ticketValue) {
        putTicketGrantingTicketIntoMap(context.getRequestScope(), ticketValue);
        putTicketGrantingTicketIntoMap(context.getFlowScope(), ticketValue);

        FlowSession session = context.getFlowExecutionContext().getActiveSession().getParent();
        while (session != null) {
            putTicketGrantingTicketIntoMap(session.getScope(), ticketValue);
            session = session.getParent();
        }
    }

    /**
     * Put ticket granting ticket into map that is either backed by the flow/request scope.
     * Will override the previous value and blank out the setting if value is null or empty.
     *
     * @param map         the map
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketIntoMap(final MutableAttributeMap map, final String ticketValue) {
        map.put(PARAMETER_TICKET_GRANTING_TICKET_ID, ticketValue);
    }

    /**
     * Gets the ticket granting ticket id from the request and flow scopes.
     *
     * @param context the context
     * @return the ticket granting ticket id
     */
    public static String getTicketGrantingTicketId(final RequestContext context) {
        final String tgtFromRequest = (String) context.getRequestScope().get(PARAMETER_TICKET_GRANTING_TICKET_ID);
        final String tgtFromFlow = (String) context.getFlowScope().get(PARAMETER_TICKET_GRANTING_TICKET_ID);

        return tgtFromRequest != null ? tgtFromRequest : tgtFromFlow;

    }

    /**
     * Put service ticket in request scope.
     *
     * @param context     the context
     * @param ticketValue the ticket value
     */
    public static void putServiceTicketInRequestScope(final RequestContext context, final ServiceTicket ticketValue) {
        context.getRequestScope().put(PARAMETER_SERVICE_TICKET_ID, ticketValue.getId());
    }

    /**
     * Gets the service ticket from request scope.
     *
     * @param context the context
     * @return the service ticket from request scope
     */
    public static String getServiceTicketFromRequestScope(final RequestContext context) {
        return context.getRequestScope().getString(PARAMETER_SERVICE_TICKET_ID);
    }

    /**
     * Adds the unauthorized redirect url to the flow scope.
     *
     * @param context the request context
     * @param url     the uri to redirect the flow
     */
    public static void putUnauthorizedRedirectUrlIntoFlowScope(final RequestContext context, final URI url) {
        context.getFlowScope().put(PARAMETER_UNAUTHORIZED_REDIRECT_URL, url);
    }

    /**
     * Put logout requests into flow scope.
     *
     * @param context  the context
     * @param requests the requests
     */
    public static void putLogoutRequests(final RequestContext context, final List<LogoutRequest> requests) {
        context.getFlowScope().put(PARAMETER_LOGOUT_REQUESTS, requests);
    }

    /**
     * Gets the logout requests from flow scope.
     *
     * @param context the context
     * @return the logout requests
     */
    public static List<LogoutRequest> getLogoutRequests(final RequestContext context) {
        return (List<LogoutRequest>) context.getFlowScope().get(PARAMETER_LOGOUT_REQUESTS);
    }

    /**
     * Put service into flowscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putService(final RequestContext context, final Service service) {
        context.getFlowScope().put(PARAMETER_SERVICE, service);
    }

    /**
     * Put warning cookie value into flowscope.
     *
     * @param context     the context
     * @param cookieValue the cookie value
     */
    public static void putWarningCookie(final RequestContext context, final Boolean cookieValue) {
        context.getFlowScope().put("warnCookieValue", cookieValue);
    }

    /**
     * Gets warning cookie.
     *
     * @param context the context
     * @return warning cookie value, if present.
     */
    public static boolean getWarningCookie(final RequestContext context) {
        final String val = ObjectUtils.defaultIfNull(context.getFlowScope().get("warnCookieValue"), Boolean.FALSE.toString()).toString();
        return Boolean.valueOf(val);
    }

    /**
     * Put registered service into flowscope.
     *
     * @param context           the context
     * @param registeredService the service
     */
    public static void putRegisteredService(final RequestContext context, final RegisteredService registeredService) {
        context.getFlowScope().put(PARAMETER_REGISTERED_SERVICE, registeredService);
    }

    /**
     * Gets credential.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the credential
     */
    public static <T extends Credential> T getCredential(final RequestContext context, final Class<T> clazz) {
        Assert.notNull(clazz, "clazz cannot be null");
        final Credential credential = getCredential(context);
        if (credential == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(credential.getClass())) {
            throw new ClassCastException("credential [" + credential.getId()
                    + " is of type " + credential.getClass()
                    + " when we were expecting " + clazz);
        }
        return (T) credential;
    }

    /**
     * Gets credential from the context.
     *
     * @param context the context
     * @return the credential, or null if it cant be found in the context or if it has no id.
     */
    public static Credential getCredential(final RequestContext context) {
        final Credential cFromRequest = (Credential) context.getRequestScope().get(PARAMETER_CREDENTIAL);
        final Credential cFromFlow = (Credential) context.getFlowScope().get(PARAMETER_CREDENTIAL);

        Credential credential = cFromRequest != null ? cFromRequest : cFromFlow;

        if (credential == null) {
            final FlowSession session = context.getFlowExecutionContext().getActiveSession();
            credential = session.getScope().get(PARAMETER_CREDENTIAL, Credential.class);
        }
        if (credential != null && StringUtils.isBlank(credential.getId())) {
            return null;
        }
        return credential;
    }

    /**
     * Puts credential into the context.
     *
     * @param context the context
     * @param c       the c
     */
    public static void putCredential(final RequestContext context, final Credential c) {
        if (c == null) {
            context.getRequestScope().remove(PARAMETER_CREDENTIAL);
            context.getFlowScope().remove(PARAMETER_CREDENTIAL);
        } else {
            context.getRequestScope().put(PARAMETER_CREDENTIAL, c);
            context.getFlowScope().put(PARAMETER_CREDENTIAL, c);
        }
    }

    /**
     * Return the username of the authenticated user (based on pac4j security).
     *
     * @return the authenticated username.
     */
    public static String getAuthenticatedUsername() {
        final HttpServletRequest request = getHttpServletRequestFromRequestAttributes();
        final HttpServletResponse response = getHttpServletResponseFromRequestAttributes();
        if (request != null && response != null) {
            final ProfileManager manager = getPac4jProfileManager(request, response);
            final Optional<UserProfile> profile = manager.get(true);
            if (profile != null && profile.isPresent()) {
                final String id = profile.get().getId();
                if (id != null) {
                    return id;
                }
            }
        }
        return PrincipalResolver.UNKNOWN_USER;
    }

    /**
     * Gets pac 4 j profile manager.
     *
     * @param request  the request
     * @param response the response
     * @return the pac 4 j profile manager
     */
    public static ProfileManager getPac4jProfileManager(final HttpServletRequest request, final HttpServletResponse response) {
        final J2EContext context = getPac4jJ2EContext(request, response);
        return getPac4jProfileManager(context);
    }

    /**
     * Gets pac4j profile manager.
     *
     * @param context the context
     * @return the pac4j profile manager
     */
    public static ProfileManager getPac4jProfileManager(final WebContext context) {
        return new ProfileManager(context);
    }

    /**
     * Gets pac4j context.
     *
     * @param request  the request
     * @param response the response
     * @return the context
     */
    public static J2EContext getPac4jJ2EContext(final HttpServletRequest request, final HttpServletResponse response) {
        return new J2EContext(request, response);
    }

    /**
     * Gets pac4j context.
     *
     * @return the pac4j context
     */
    public static J2EContext getPac4jJ2EContext() {
        return getPac4jJ2EContext(getHttpServletRequestFromRequestAttributes(), getHttpServletResponseFromRequestAttributes());
    }

    /**
     * Is authenticating at a public workstation?
     *
     * @param ctx the ctx
     * @return true if the cookie value is present
     */
    public static boolean isAuthenticatingAtPublicWorkstation(final RequestContext ctx) {
        if (ctx.getFlowScope().contains(PUBLIC_WORKSTATION_ATTRIBUTE)) {
            LOGGER.debug("Public workstation flag detected. SSO session will be considered renewed.");
            return true;
        }
        return false;
    }


    /**
     * Put public workstation into the flow if request parameter present.
     *
     * @param context the context
     */
    public static void putPublicWorkstationToFlowIfRequestParameterPresent(final RequestContext context) {
        if (StringUtils.isNotBlank(context.getExternalContext().getRequestParameterMap().get(PUBLIC_WORKSTATION_ATTRIBUTE))) {
            context.getFlowScope().put(PUBLIC_WORKSTATION_ATTRIBUTE, Boolean.TRUE);
        }
    }

    /**
     * Put warn cookie if request parameter present.
     *
     * @param warnCookieGenerator the warn cookie generator
     * @param context             the context
     */
    public static void putWarnCookieIfRequestParameterPresent(final CookieGenerator warnCookieGenerator, final RequestContext context) {
        if (warnCookieGenerator != null) {
            LOGGER.debug("Evaluating request to determine if warning cookie should be generated");
            final HttpServletResponse response = WebUtils.getHttpServletResponse(context);
            if (StringUtils.isNotBlank(context.getExternalContext().getRequestParameterMap().get("warn"))) {
                warnCookieGenerator.addCookie(response, "true");
            }
        } else {
            LOGGER.debug("No warning cookie generator is defined");
        }
    }

    /**
     * Put authentication into conversation scope.
     *
     * @param authentication the authentication
     * @param ctx            the ctx
     */
    public static void putAuthentication(final Authentication authentication, final RequestContext ctx) {
        ctx.getConversationScope().put(PARAMETER_AUTHENTICATION, authentication);
    }

    /**
     * Gets authentication from conversation scope.
     *
     * @param ctx the ctx
     * @return the authentication
     */
    public static Authentication getAuthentication(final RequestContext ctx) {
        return ctx.getConversationScope().get(PARAMETER_AUTHENTICATION, Authentication.class);
    }

    /**
     * Put authentication result builder.
     *
     * @param builder the builder
     * @param ctx     the ctx
     */
    public static void putAuthenticationResultBuilder(final AuthenticationResultBuilder builder, final RequestContext ctx) {
        ctx.getConversationScope().put(PARAMETER_AUTHENTICATION_RESULT_BUILDER, builder);
    }

    /**
     * Gets the authenticated principal.
     *
     * @param requestContext        the request context
     * @param ticketRegistrySupport the ticket registry support
     * @return the principal
     */
    public static Principal getPrincipalFromRequestContext(final RequestContext requestContext,
                                                           final TicketRegistrySupport ticketRegistrySupport) {
        final String tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgt)) {
            throw new IllegalArgumentException("No ticket-granting ticket could be found in the context");
        }

        return ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgt);
    }

    /**
     * Gets authentication result builder.
     *
     * @param ctx the ctx
     * @return the authentication result builder
     */
    public static AuthenticationResultBuilder getAuthenticationResultBuilder(final RequestContext ctx) {
        return ctx.getConversationScope().get(PARAMETER_AUTHENTICATION_RESULT_BUILDER, AuthenticationResultBuilder.class);
    }

    /**
     * Put authentication result.
     *
     * @param authenticationResult the authentication result
     * @param context              the context
     */
    public static void putAuthenticationResult(final AuthenticationResult authenticationResult, final RequestContext context) {
        context.getConversationScope().put(PARAMETER_AUTHENTICATION_RESULT, authenticationResult);
    }

    /**
     * Gets authentication result builder.
     *
     * @param ctx the ctx
     * @return the authentication context builder
     */
    public static AuthenticationResult getAuthenticationResult(final RequestContext ctx) {
        return ctx.getConversationScope().get(PARAMETER_AUTHENTICATION_RESULT, AuthenticationResult.class);
    }

    /**
     * Gets http servlet request user agent.
     *
     * @param request the request
     * @return the http servlet request user agent
     */
    public static String getHttpServletRequestUserAgent(final HttpServletRequest request) {
        if (request != null) {
            return request.getHeader(USER_AGENT_HEADER);
        }
        return null;
    }

    /**
     * Gets http servlet request user agent.
     *
     * @return the http servlet request user agent
     */
    public static String getHttpServletRequestUserAgent() {
        return getHttpServletRequestUserAgent(getHttpServletRequest());
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param request the request
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation(final HttpServletRequest request) {
        final int latIndex = 0;
        final int longIndex = 1;
        final int accuracyIndex = 2;
        final int timeIndex = 3;
        final GeoLocationRequest loc = new GeoLocationRequest();
        if (request != null) {
            final String geoLocationParam = request.getParameter("geolocation");
            if (StringUtils.isNotBlank(geoLocationParam)) {
                final String[] geoLocation = geoLocationParam.split(",");
                loc.setLatitude(geoLocation[latIndex]);
                loc.setLongitude(geoLocation[longIndex]);
                loc.setAccuracy(geoLocation[accuracyIndex]);
                loc.setTimestamp(geoLocation[timeIndex]);
            }
        }
        return loc;
    }

    /**
     * Gets http servlet request geo location.
     *
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation() {
        return getHttpServletRequestGeoLocation(WebUtils.getHttpServletRequest());
    }

    /**
     * Put geo location tracking into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putGeoLocationTrackingIntoFlowScope(final RequestContext context, final Object value) {
        context.getFlowScope().put("trackGeoLocation", value);
    }

    /**
     * Put recaptcha site key into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putRecaptchaSiteKeyIntoFlowScope(final RequestContext context, final Object value) {
        context.getFlowScope().put("recaptchaSiteKey", value);
    }

    /**
     * Put static authentication into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putStaticAuthenticationIntoFlowScope(final RequestContext context, final Object value) {
        context.getFlowScope().put("staticAuthentication", value);
    }

    /**
     * Put static authentication into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putPasswordManagementEnabled(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("passwordManagementEnabled", value);
    }

    /**
     * Put tracking id into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putGoogleAnalyticsTrackingIdIntoFlowScope(final RequestContext context, final Object value) {
        context.getFlowScope().put("googleAnalyticsTrackingId", value);
    }

    /**
     * Put unauthorized redirect url into flowscope.
     *
     * @param context                 the context
     * @param unauthorizedRedirectUrl the url to redirect to
     */
    public static void putUnauthorizedRedirectUrl(final RequestContext context, final URI unauthorizedRedirectUrl) {
        context.getFlowScope().put(PARAMETER_UNAUTHORIZED_REDIRECT_URL, unauthorizedRedirectUrl);
    }

    /**
     * Put principal.
     *
     * @param requestContext          the request context
     * @param authenticationPrincipal the authentication principal
     */
    public static void putPrincipal(final RequestContext requestContext, final Principal authenticationPrincipal) {
        requestContext.getFlowScope().put("principal", authenticationPrincipal);
    }

    /**
     * Put logout redirect url.
     *
     * @param context the context
     * @param service the service
     */
    public static void putLogoutRedirectUrl(final RequestContext context, final String service) {
        context.getFlowScope().put("logoutRedirectUrl", service);
    }

    /**
     * Put remember me authentication enabled.
     *
     * @param context the context
     * @param enabled the enabled
     */
    public static void putRememberMeAuthenticationEnabled(final RequestContext context, final Boolean enabled) {
        context.getFlowScope().put("rememberMeAuthenticationEnabled", enabled);
    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @param applicationContext the application context
     * @return the all multifactor authentication providers from application context
     */
    public static Map<String, MultifactorAuthenticationProvider> getAvailableMultifactorAuthenticationProviders(
            final ApplicationContext applicationContext) {
        try {
            return applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class, false, true);
        } catch (final Exception e) {
            LOGGER.warn("Could not locate beans of type [{}]", MultifactorAuthenticationProvider.class);
        }
        return Collections.emptyMap();
    }

    /**
     * Put resolved multifactor authentication providers into scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putResolvedMultifactorAuthenticationProviders(final RequestContext context,
                                                                     final Collection<MultifactorAuthenticationProvider> value) {
        context.getConversationScope().put("resolvedMultifactorAuthenticationProviders", value);
    }

    /**
     * Gets resolved multifactor authentication providers.
     *
     * @param context the context
     * @return the resolved multifactor authentication providers
     */
    public static Collection<MultifactorAuthenticationProvider> getResolvedMultifactorAuthenticationProviders(final RequestContext context) {
        return context.getConversationScope().get("resolvedMultifactorAuthenticationProviders", Collection.class);
    }

    /**
     * Sets service user interface metadata.
     *
     * @param requestContext the request context
     * @param mdui           the mdui
     */
    public static void putServiceUserInterfaceMetadata(final RequestContext requestContext, final Serializable mdui) {
        if (mdui != null) {
            requestContext.getFlowScope().put(PARAMETER_SERVICE_UI_METADATA, mdui);
        }
    }

    /**
     * Gets service user interface metadata.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clz            the clz
     * @return the service user interface metadata
     */
    public static <T> T getServiceUserInterfaceMetadata(final RequestContext requestContext, final Class<T> clz) {
        if (requestContext.getFlowScope().contains(PARAMETER_SERVICE_UI_METADATA)) {
            return requestContext.getFlowScope().get(PARAMETER_SERVICE_UI_METADATA, clz);
        }
        return null;
    }

    /**
     * Put service response into request scope.
     *
     * @param requestContext the request context
     * @param response       the response
     */
    public static void putServiceResponseIntoRequestScope(final RequestContext requestContext, final Response response) {
        requestContext.getRequestScope().put("parameters", response.getAttributes());
        requestContext.getRequestScope().put("url", response.getUrl());
    }

    /**
     * Put service original url into request scope.
     *
     * @param requestContext the request context
     * @param service        the service
     */
    public static void putServiceOriginalUrlIntoRequestScope(final RequestContext requestContext, final WebApplicationService service) {
        requestContext.getRequestScope().put("originalUrl", service.getOriginalUrl());
    }

    /**
     * Produce unauthorized error view model and view.
     *
     * @return the model and view
     */
    public static ModelAndView produceUnauthorizedErrorView() {
        return produceErrorView(new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY));
    }

    /**
     * Produce error view model and view.
     *
     * @param e the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Exception e) {
        final Map model = new HashMap<>();
        model.put("rootCauseException", e);
        return new ModelAndView(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, model);
    }
}
