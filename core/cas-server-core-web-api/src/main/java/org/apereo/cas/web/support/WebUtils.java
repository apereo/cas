package org.apereo.cas.web.support;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Common utilities for the web tier.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@UtilityClass
public class WebUtils {
    /**
     * Ticket-granting ticket id parameter used in various flow scopes.
     */
    public static final String PARAMETER_TICKET_GRANTING_TICKET_ID = "ticketGrantingTicketId";

    private static final String PUBLIC_WORKSTATION_ATTRIBUTE = "publicWorkstation";

    private static final String PARAMETER_AUTHENTICATION = "authentication";

    private static final String PARAMETER_AUTHENTICATION_RESULT_BUILDER = "authenticationResultBuilder";

    private static final String PARAMETER_AUTHENTICATION_RESULT = "authenticationResult";

    private static final String PARAMETER_CREDENTIAL = "credential";

    private static final String PARAMETER_UNAUTHORIZED_REDIRECT_URL = "unauthorizedRedirectUrl";

    private static final String PARAMETER_REGISTERED_SERVICE = "registeredService";

    private static final String PARAMETER_SERVICE = "service";

    private static final String PARAMETER_SERVICE_TICKET_ID = "serviceTicketId";

    private static final String PARAMETER_LOGOUT_REQUESTS = "logoutRequests";

    private static final String PARAMETER_SERVICE_UI_METADATA = "serviceUIMetadata";

    /**
     * Gets resolved events as attribute.
     *
     * @param context the context
     * @return the resolved events as attribute
     */
    public static Collection<Event> getResolvedEventsAsAttribute(final RequestContext context) {
        return context.getAttributes().get("resolvedAuthenticationEvents", Collection.class);
    }

    /**
     * Put resolved events as attribute.
     *
     * @param context        the context
     * @param resolvedEvents the resolved events
     */
    public static void putResolvedEventsAsAttribute(final RequestContext context,
                                                    final Collection<Event> resolvedEvents) {
        context.getAttributes().put("resolvedAuthenticationEvents", resolvedEvents);
    }

    /**
     * Gets the http servlet request from the context.
     *
     * @param context the context
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequestFromExternalWebflowContext(final RequestContext context) {
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
    public static HttpServletRequest getHttpServletRequestFromExternalWebflowContext() {
        val servletExternalContext = (ServletExternalContext) ExternalContextHolder.getExternalContext();
        if (servletExternalContext != null) {
            return (HttpServletRequest) servletExternalContext.getNativeRequest();
        }
        return null;

    }

    /**
     * Gets the http servlet response from the context.
     *
     * @param context the context
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponseFromExternalWebflowContext(final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context.getExternalContext(),
            "Cannot obtain HttpServletResponse from event of type: " + context.getExternalContext().getClass().getName());
        return (HttpServletResponse) context.getExternalContext().getNativeResponse();
    }

    /**
     * Gets the http servlet response from the current servlet context.
     *
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponseFromExternalWebflowContext() {
        val servletExternalContext = (ServletExternalContext) ExternalContextHolder.getExternalContext();
        if (servletExternalContext != null) {
            return (HttpServletResponse) servletExternalContext.getNativeResponse();
        }
        return null;
    }


    /**
     * Gets the service.
     *
     * @param argumentExtractors the argument extractors
     * @param context            the context
     * @return the service
     */
    public static WebApplicationService getService(final List<ArgumentExtractor> argumentExtractors, final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        return HttpRequestUtils.getService(argumentExtractors, request);
    }

    /**
     * Gets the service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static WebApplicationService getService(final RequestContext context) {
        return Optional.ofNullable(context).map(requestContext -> (WebApplicationService) requestContext.getFlowScope().get(PARAMETER_SERVICE)).orElse(null);
    }

    /**
     * Gets the registered service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static RegisteredService getRegisteredService(final RequestContext context) {
        return Optional.ofNullable(context)
            .map(requestContext -> (RegisteredService) requestContext.getFlowScope().get(PARAMETER_REGISTERED_SERVICE)).orElse(null);
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticket  the ticket value
     */
    public static void putTicketGrantingTicketInScopes(final RequestContext context, final TicketGrantingTicket ticket) {
        val ticketValue = Optional.ofNullable(ticket).map(Ticket::getId).orElse(null);
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

        var session = context.getFlowExecutionContext().getActiveSession().getParent();
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
        val tgtFromRequest = getTicketGrantingTicketIdFrom(context.getRequestScope());
        val tgtFromFlow = getTicketGrantingTicketIdFrom(context.getFlowScope());
        return Optional.ofNullable(tgtFromRequest).orElse(tgtFromFlow);
    }

    /**
     * Gets ticket granting ticket id from.
     *
     * @param scopeMap the scope map
     * @return the ticket granting ticket id from
     */
    public static String getTicketGrantingTicketIdFrom(final MutableAttributeMap scopeMap) {
        return (String) scopeMap.get(PARAMETER_TICKET_GRANTING_TICKET_ID);
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
     * Gets unauthorized redirect url into flow scope.
     *
     * @param context the context
     * @return the unauthorized redirect url into flow scope
     */
    public static URI getUnauthorizedRedirectUrlFromFlowScope(final RequestContext context) {
        return context.getFlowScope().get(PARAMETER_UNAUTHORIZED_REDIRECT_URL, URI.class);
    }

    /**
     * Put logout requests into flow scope.
     *
     * @param context  the context
     * @param requests the requests
     */
    public static void putLogoutRequests(final RequestContext context, final List<SingleLogoutRequest> requests) {
        context.getFlowScope().put(PARAMETER_LOGOUT_REQUESTS, requests);
    }

    /**
     * Put logout urls into flow scope.
     *
     * @param context the context
     * @param urls    the requests
     */
    public static void putLogoutUrls(final RequestContext context, final Map urls) {
        context.getFlowScope().put("logoutUrls", urls);
    }

    /**
     * Gets the logout requests from flow scope.
     *
     * @param context the context
     * @return the logout requests
     */
    public static List<SingleLogoutRequest> getLogoutRequests(final RequestContext context) {
        return (List<SingleLogoutRequest>) context.getFlowScope().get(PARAMETER_LOGOUT_REQUESTS);
    }

    /**
     * Put service into flowscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putServiceIntoFlowScope(final RequestContext context, final Service service) {
        context.getFlowScope().put(PARAMETER_SERVICE, service);
    }

    /**
     * Put service into flashscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putServiceIntoFlashScope(final RequestContext context, final Service service) {
        context.getFlashScope().put(PARAMETER_SERVICE, service);
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
        val val = ObjectUtils.defaultIfNull(context.getFlowScope().get("warnCookieValue"), Boolean.FALSE.toString()).toString();
        return Boolean.parseBoolean(val);
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
    public static <T extends Credential> T getCredential(final RequestContext context, final @NonNull Class<T> clazz) {
        val credential = getCredential(context);
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
        val cFromRequest = (Credential) context.getRequestScope().get(PARAMETER_CREDENTIAL);
        val cFromFlow = (Credential) context.getFlowScope().get(PARAMETER_CREDENTIAL);
        val cFromConversation = (Credential) context.getConversationScope().get(PARAMETER_CREDENTIAL);

        var credential = cFromRequest;
        if (credential == null || StringUtils.isBlank(credential.getId())) {
            credential = cFromFlow;
        }
        if (credential == null || StringUtils.isBlank(credential.getId())) {
            credential = cFromConversation;
            if (credential != null && !StringUtils.isBlank(credential.getId())) {
                context.getFlowScope().put(PARAMETER_CREDENTIAL, credential);
            }
        }

        if (credential == null) {
            val session = context.getFlowExecutionContext().getActiveSession();
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
            context.getConversationScope().remove(PARAMETER_CREDENTIAL);
        } else {
            context.getRequestScope().put(PARAMETER_CREDENTIAL, c);
            context.getFlowScope().put(PARAMETER_CREDENTIAL, c);
            context.getConversationScope().put(PARAMETER_CREDENTIAL, c);
        }
    }

    /**
     * Remove credential.
     *
     * @param context the context
     */
    public static void removeCredential(final RequestContext context) {
        putCredential(context, null);
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
    public static void putWarnCookieIfRequestParameterPresent(final CasCookieBuilder warnCookieGenerator, final RequestContext context) {
        if (warnCookieGenerator != null) {
            LOGGER.trace("Evaluating request to determine if warning cookie should be generated");
            if (StringUtils.isNotBlank(context.getExternalContext().getRequestParameterMap().get("warn"))) {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                warnCookieGenerator.addCookie(response, "true");
            }
        } else {
            LOGGER.trace("No warning cookie generator is defined");
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
        val tgt = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(tgt)) {
            throw new IllegalArgumentException("No ticket-granting ticket could be found in the context");
        }

        return ticketRegistrySupport.getAuthenticatedPrincipalFrom(tgt);
    }

    /**
     * Gets principal from request context.
     *
     * @param requestContext the request context
     * @return the principal from request context
     */
    public static Principal getPrincipalFromRequestContext(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("principal", Principal.class);
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
     * @return the http servlet request user agent
     */
    public static String getHttpServletRequestUserAgentFromRequestContext() {
        val request = getHttpServletRequestFromExternalWebflowContext();
        return HttpRequestUtils.getHttpServletRequestUserAgent(request);
    }

    /**
     * Gets http servlet request user agent from request context.
     *
     * @param context the context
     * @return the http servlet request user agent from request context
     */
    public static String getHttpServletRequestUserAgentFromRequestContext(final RequestContext context) {
        val request = getHttpServletRequestFromExternalWebflowContext(context);
        return getHttpServletRequestUserAgentFromRequestContext(request);
    }

    /**
     * Gets http servlet request user agent from request context.
     *
     * @param request the request
     * @return the http servlet request user agent from request context
     */
    public static String getHttpServletRequestUserAgentFromRequestContext(final HttpServletRequest request) {
        return HttpRequestUtils.getHttpServletRequestUserAgent(request);
    }

    /**
     * Gets http servlet request geo location.
     *
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocationFromRequestContext() {
        val servletRequest = getHttpServletRequestFromExternalWebflowContext();
        return getHttpServletRequestGeoLocation(servletRequest);
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param context the context
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocationFromRequestContext(final RequestContext context) {
        val servletRequest = getHttpServletRequestFromExternalWebflowContext(context);
        return getHttpServletRequestGeoLocation(servletRequest);
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param servletRequest the servlet request
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation(final HttpServletRequest servletRequest) {
        if (servletRequest != null) {
            return HttpRequestUtils.getHttpServletRequestGeoLocation(servletRequest);
        }
        return null;
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
     * Put recaptcha settings flow scope.
     *
     * @param context         the context
     * @param googleRecaptcha the properties
     */
    public static void putRecaptchaPropertiesFlowScope(final RequestContext context, final GoogleRecaptchaProperties googleRecaptcha) {
        val flowScope = context.getFlowScope();
        flowScope.put("recaptchaSiteKey", googleRecaptcha.getSiteKey());
        flowScope.put("recaptchaInvisible", googleRecaptcha.isInvisible());
        flowScope.put("recaptchaPosition", googleRecaptcha.getPosition());
        flowScope.put("recaptchaVersion", googleRecaptcha.getVersion().name().toLowerCase());
    }

    /**
     * Gets recaptcha site key.
     *
     * @param context the context
     * @return the recaptcha site key
     */
    public static String getRecaptchaSiteKey(final RequestContext context) {
        val flowScope = context.getFlowScope();
        return flowScope.get("recaptchaSiteKey", String.class);
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
     * Is remember me authentication enabled ?.
     *
     * @param context the context
     * @return true/false
     */
    public static Boolean isRememberMeAuthenticationEnabled(final RequestContext context) {
        return context.getFlowScope().getBoolean("rememberMeAuthenticationEnabled", Boolean.FALSE);
    }

    /**
     * Gets multifactor authentication trust record.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the multifactor authentication trust record
     */
    public static <T> Optional<T> getMultifactorAuthenticationTrustRecord(final RequestContext context, final Class<T> clazz) {
        return Optional.ofNullable(context.getFlowScope().get(CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, clazz));
    }

    /**
     * Put multifactor authentication trust record.
     *
     * @param context the context
     * @param object  the object
     */
    public static void putMultifactorAuthenticationTrustRecord(final RequestContext context, final Serializable object) {
        context.getFlowScope().put(CasWebflowConstants.VAR_ID_MFA_TRUST_RECORD, object);
    }

    /**
     * Put resolved multifactor authentication providers into scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putResolvedMultifactorAuthenticationProviders(final RequestContext context,
                                                                     final Collection<MultifactorAuthenticationProvider> value) {
        val providerIds = value.stream().map(MultifactorAuthenticationProvider::getId).collect(Collectors.toSet());
        context.getConversationScope().put("resolvedMultifactorAuthenticationProviders", providerIds);
    }

    /**
     * Gets resolved multifactor authentication providers.
     *
     * @param context the context
     * @return the resolved multifactor authentication providers
     */
    public static Collection<String> getResolvedMultifactorAuthenticationProviders(final RequestContext context) {
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
     * @param url            the url
     */
    public static void putServiceRedirectUrl(final RequestContext requestContext, final String url) {
        requestContext.getRequestScope().put("url", url);
    }

    /**
     * Put service response into request scope.
     *
     * @param requestContext the request context
     * @param response       the response
     */
    public static void putServiceResponseIntoRequestScope(final RequestContext requestContext, final Response response) {
        requestContext.getRequestScope().put("parameters", response.getAttributes());
        putServiceRedirectUrl(requestContext, response.getUrl());
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
     * @param view the view
     * @param e    the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final String view, final Exception e) {
        return new ModelAndView(view, CollectionUtils.wrap("rootCauseException", e));
    }

    /**
     * Produce error view model and view.
     *
     * @param e the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Exception e) {
        return produceErrorView(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, e);
    }

    /**
     * Gets in progress authentication.
     *
     * @return the in progress authentication
     */
    public static Authentication getInProgressAuthentication() {
        val context = RequestContextHolder.getRequestContext();
        val authentication = Optional.ofNullable(context).map(WebUtils::getAuthentication).orElse(null);
        if (authentication == null) {
            return AuthenticationCredentialsThreadLocalBinder.getInProgressAuthentication();
        }
        return authentication;
    }

    /**
     * Put passwordless authentication enabled.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putPasswordlessAuthenticationEnabled(final RequestContext requestContext, final Boolean value) {
        requestContext.getFlowScope().put("passwordlessAuthenticationEnabled", value);
    }

    /**
     * Put passwordless authentication account.
     *
     * @param requestContext the request context
     * @param account        the account
     */
    public static void putPasswordlessAuthenticationAccount(final RequestContext requestContext, final Object account) {
        requestContext.getFlowScope().put("passwordlessAccount", account);
    }

    /**
     * Gets passwordless authentication account.
     *
     * @param <T>   the type parameter
     * @param event the event
     * @param clazz the clazz
     * @return the passwordless authentication account
     */
    public static <T> T getPasswordlessAuthenticationAccount(final Event event, final Class<T> clazz) {
        if (event != null) {
            return event.getAttributes().get("passwordlessAccount", clazz);
        }
        return null;
    }

    /**
     * Gets passwordless authentication account.
     *
     * @param <T>            the type parameter
     * @param requestContext the context
     * @param clazz          the clazz
     * @return the passwordless authentication account
     */
    public static <T> T getPasswordlessAuthenticationAccount(final RequestContext requestContext, final Class<T> clazz) {
        var result = getPasswordlessAuthenticationAccount(requestContext.getCurrentEvent(), clazz);
        if (result == null) {
            result = requestContext.getFlowScope().get("passwordlessAccount", clazz);
        }
        return result;
    }

    /**
     * Has passwordless authentication account.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean hasPasswordlessAuthenticationAccount(final RequestContext requestContext) {
        return requestContext.getFlowScope().contains("passwordlessAccount");
    }

    /**
     * Put request surrogate authentication.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putRequestSurrogateAuthentication(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("requestSurrogateAccount", value);
    }

    /**
     * Has request surrogate authentication request.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean hasRequestSurrogateAuthenticationRequest(final RequestContext requestContext) {
        return requestContext.getFlowScope().getBoolean("requestSurrogateAccount", Boolean.FALSE);
    }

    /**
     * Has request surrogate authentication request.
     *
     * @param requestContext the request context
     */
    public static void removeRequestSurrogateAuthenticationRequest(final RequestContext requestContext) {
        requestContext.getFlowScope().remove("requestSurrogateAccount");
    }

    /**
     * Put surrogate authentication accounts.
     *
     * @param requestContext the request context
     * @param surrogates     the surrogates
     */
    public static void putSurrogateAuthenticationAccounts(final RequestContext requestContext, final List<String> surrogates) {
        requestContext.getFlowScope().put("surrogates", surrogates);
    }

    /**
     * Gets surrogate authentication accounts.
     *
     * @param requestContext the request context
     * @return the surrogate authentication accounts
     */
    public static List<String> getSurrogateAuthenticationAccounts(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("surrogates", List.class);
    }

    /**
     * Put graphical user authentication enabled.
     *
     * @param requestContext the request context
     * @param value          the value
     */
    public static void putGraphicalUserAuthenticationEnabled(final RequestContext requestContext, final Boolean value) {
        requestContext.getFlowScope().put("guaEnabled", value);
    }

    /**
     * Put graphical user authentication enabled.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isGraphicalUserAuthenticationEnabled(final RequestContext requestContext) {
        return BooleanUtils.isTrue(requestContext.getFlowScope().get("guaEnabled", Boolean.class));
    }

    /**
     * Put graphical user authentication username.
     *
     * @param requestContext the request context
     * @param username       the username
     */
    public static void putGraphicalUserAuthenticationUsername(final RequestContext requestContext, final String username) {
        requestContext.getFlowScope().put("guaUsername", username);
    }

    /**
     * Contains graphical user authentication username.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean containsGraphicalUserAuthenticationUsername(final RequestContext requestContext) {
        return requestContext.getFlowScope().contains("guaUsername");
    }

    /**
     * Put graphical user authentication image.
     *
     * @param requestContext the request context
     * @param image          the image
     */
    public static void putGraphicalUserAuthenticationImage(final RequestContext requestContext, final String image) {
        requestContext.getFlowScope().put("guaUserImage", image);
    }

    /**
     * Contains graphical user authentication image boolean.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean containsGraphicalUserAuthenticationImage(final RequestContext requestContext) {
        return requestContext.getFlowScope().contains("guaUserImage");
    }

    /**
     * Put delegated authentication provider dominant.
     *
     * @param context the context
     * @param client  the client
     */
    public static void putDelegatedAuthenticationProviderPrimary(final RequestContext context, final Object client) {
        context.getFlowScope().put("delegatedAuthenticationProviderPrimary", client);
    }

    /**
     * Gets delegated authentication provider primary.
     *
     * @param context the context
     * @return the delegated authentication provider primary
     */
    public static Object getDelegatedAuthenticationProviderPrimary(final RequestContext context) {
        return context.getFlowScope().get("delegatedAuthenticationProviderPrimary");
    }

    /**
     * Put available authentication handle names.
     *
     * @param context           the context
     * @param availableHandlers the available handlers
     */
    public static void putAvailableAuthenticationHandleNames(final RequestContext context, final Collection<String> availableHandlers) {
        context.getFlowScope().put("availableAuthenticationHandlerNames", availableHandlers);
    }

    /**
     * Put acceptable usage policy status into flow scope.
     *
     * @param context the context
     * @param status  the status
     */
    public static void putAcceptableUsagePolicyStatusIntoFlowScope(final RequestContext context, final Object status) {
        context.getFlowScope().put("aupStatus", status);
    }

    /**
     * Put acceptable usage policy terms into flow scope.
     *
     * @param context the context
     * @param terms   the terms
     */
    public static void putAcceptableUsagePolicyTermsIntoFlowScope(final RequestContext context, final Object terms) {
        context.getFlowScope().put("aupPolicy", terms);
    }

    /**
     * Gets acceptable usage policy terms from flow scope.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the acceptable usage policy terms from flow scope
     */
    public static <T> T getAcceptableUsagePolicyTermsFromFlowScope(final RequestContext requestContext, final Class<T> clazz) {
        if (requestContext.getFlowScope().contains("aupPolicy")) {
            return (T) requestContext.getFlowScope().put("aupPolicy", clazz);
        }
        return null;
    }

    /**
     * Put custom login form fields.
     *
     * @param context               the context
     * @param customLoginFormFields the custom login form fields
     */
    public static void putCustomLoginFormFields(final RequestContext context, final Map customLoginFormFields) {
        context.getFlowScope().put("customLoginFormFields", customLoginFormFields);
    }

    /**
     * Put initial http request post parameters.
     *
     * @param context the context
     */
    public static void putInitialHttpRequestPostParameters(final RequestContext context) {
        val request = getHttpServletRequestFromExternalWebflowContext(context);
        context.getFlashScope().put("httpRequestInitialPostParameters", request.getParameterMap());
    }

    /**
     * Put existing single sign on session available.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putExistingSingleSignOnSessionAvailable(final RequestContext context, final boolean value) {
        context.getFlowScope().put("existingSingleSignOnSessionAvailable", value);
    }

    /**
     * Put existing single sign on session principal.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putExistingSingleSignOnSessionPrincipal(final RequestContext context, final Principal value) {
        context.getFlashScope().put("existingSingleSignOnSessionPrincipal", value);
    }

    /**
     * Put cas login form viewable.
     *
     * @param context  the context
     * @param viewable the viewable
     */
    public static void putCasLoginFormViewable(final RequestContext context, final boolean viewable) {
        context.getFlowScope().put("casLoginFormViewable", viewable);
    }

    /**
     * Is cas login form viewable.
     *
     * @param context the context
     * @return true/false
     */
    public static boolean isCasLoginFormViewable(final RequestContext context) {
        return context.getFlowScope().getBoolean("casLoginFormViewable", Boolean.TRUE);
    }

    /**
     * Gets http request full url.
     *
     * @param requestContext the request context
     * @return the http request full url
     */
    public static String getHttpRequestFullUrl(final RequestContext requestContext) {
        return getHttpRequestFullUrl(getHttpServletRequestFromExternalWebflowContext(requestContext));
    }

    /**
     * Gets http request full url.
     *
     * @param request the request
     * @return the http request full url
     */
    public static String getHttpRequestFullUrl(final HttpServletRequest request) {
        val requestURL = request.getRequestURL();
        val queryString = request.getQueryString();
        return queryString == null
            ? requestURL.toString()
            : requestURL.append('?').append(queryString).toString();
    }

    /**
     * Create credential.
     *
     * @param requestContext the request context
     */
    public static void createCredential(final RequestContext requestContext) {
        removeCredential(requestContext);
        val flow = (Flow) requestContext.getActiveFlow();
        val var = flow.getVariable(CasWebflowConstants.VAR_ID_CREDENTIAL);
        var.create(requestContext);
    }

    /**
     * Put delegated authentication provider configurations.
     *
     * @param context the context
     * @param urls    the urls
     */
    public static void putDelegatedAuthenticationProviderConfigurations(final RequestContext context,
                                                                        final Set<? extends Serializable> urls) {
        context.getFlowScope().put("delegatedAuthenticationProviderConfigurations", urls);
    }

    /**
     * Gets delegated authentication provider configurations.
     *
     * @param context the context
     * @return the delegated authentication provider configurations
     */
    public static Set<? extends Serializable> getDelegatedAuthenticationProviderConfigurations(final RequestContext context) {
        val scope = context.getFlowScope();
        if (scope.contains("delegatedAuthenticationProviderConfigurations", Set.class)) {
            return scope.get("delegatedAuthenticationProviderConfigurations", Set.class);
        }
        return new HashSet<>(0);
    }

    /**
     * Put open id local user id.
     *
     * @param context the context
     * @param user    the user
     */
    public static void putOpenIdLocalUserId(final RequestContext context, final String user) {
        if (StringUtils.isBlank(user)) {
            context.getFlowScope().remove("openIdLocalId");
        } else {
            context.getFlowScope().put("openIdLocalId", user);
        }
    }

    /**
     * Gets open id local user id.
     *
     * @param context the context
     * @return the open id local user id
     * @deprecated Since 6.2.0
     */
    @Deprecated(since = "6.2.0")
    public static String getOpenIdLocalUserId(final RequestContext context) {
        return context.getFlowScope().get("openIdLocalId", String.class);
    }

    /**
     * Add the mfa provider id into flow scope.
     *
     * @param context  request context
     * @param provider the mfa provider
     */
    public static void putMultifactorAuthenticationProviderIdIntoFlowScope(final RequestContext context, final MultifactorAuthenticationProvider provider) {
        context.getFlowScope().put(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID, provider.getId());
    }

    /**
     * Get the mfa provider id from flow scope.
     *
     * @param context request context
     * @return provider id
     */
    public static String getMultifactorAuthenticationProviderById(final RequestContext context) {
        return context.getFlowScope().get(CasWebflowConstants.VAR_ID_MFA_PROVIDER_ID, String.class);
    }

    /**
     * Put selectable multifactor authentication providers.
     *
     * @param requestContext the request context
     * @param mfaProviders   the mfa providers
     */
    public static void putSelectableMultifactorAuthenticationProviders(final RequestContext requestContext, final List<String> mfaProviders) {
        requestContext.getViewScope().put("mfaSelectableProviders", mfaProviders);
    }

    /**
     * Gets selectable multifactor authentication providers.
     *
     * @param requestContext the request context
     * @return the selectable multifactor authentication providers
     */
    public static List<String> getSelectableMultifactorAuthenticationProviders(final RequestContext requestContext) {
        return requestContext.getViewScope().get("mfaSelectableProviders", List.class);
    }
}
