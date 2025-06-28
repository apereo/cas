package org.apereo.cas.web.support;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHolder;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.logout.slo.SingleLogoutRequestContext;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.WWWFormCodec;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
     * Flow attribute to indicate surrogate authn is requested..
     */
    public static final String REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE = "requestSurrogateAccount";
    /**
     * Parameter to indicate logout request is confirmed.
     */
    public static final String REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED = "LogoutRequestConfirmed";

    /**
     * Ticket-granting ticket id parameter used in various flow scopes.
     */
    public static final String PARAMETER_TICKET_GRANTING_TICKET_ID = "ticketGrantingTicketId";
    /**
     * Unauthorized redirect URL, typically the result of access strategy, used in various flow scopes.
     */
    public static final String PARAMETER_UNAUTHORIZED_REDIRECT_URL = "unauthorizedRedirectUrl";

    private static final String PARAMETER_CREDENTIAL = "credential";

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
        return (HttpServletRequest) context.getExternalContext().getNativeRequest();
    }

    /**
     * Gets the http servlet request from the current servlet context.
     *
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequestFromExternalWebflowContext() {
        val servletExternalContext = ExternalContextHolder.getExternalContext();
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
        return (HttpServletResponse) context.getExternalContext().getNativeResponse();
    }

    /**
     * Gets the http servlet response from the current servlet context.
     *
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponseFromExternalWebflowContext() {
        val servletExternalContext = ExternalContextHolder.getExternalContext();
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
        return Optional.ofNullable(context).map(requestContext
            -> (WebApplicationService) requestContext.getFlowScope().get(CasWebflowConstants.ATTRIBUTE_SERVICE)).orElse(null);
    }

    /**
     * Gets the registered service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static RegisteredService getRegisteredService(final RequestContext context) {
        return Optional.ofNullable(context)
            .map(requestContext -> (RegisteredService)
                requestContext.getFlowScope().get(CasWebflowConstants.ATTRIBUTE_REGISTERED_SERVICE)).orElse(null);
    }

    /**
     * Gets registered service.
     *
     * @param request the request
     * @return the registered service
     */
    public static RegisteredService getRegisteredService(final HttpServletRequest request) {
        return Optional.ofNullable(request)
            .map(requestContext -> (RegisteredService) request.getAttribute(CasWebflowConstants.ATTRIBUTE_REGISTERED_SERVICE)).orElse(null);
    }

    /**
     * Put ticket granting ticket.
     *
     * @param context the context
     * @param ticket  the ticket value
     */
    public static void putTicketGrantingTicket(final RequestContext context, final Ticket ticket) {
        context.getFlowScope().put("ticketGrantingTicket", ticket);
    }

    /**
     * Get ticket granting ticket.
     *
     * @param context the context
     * @return the ticket granting ticket
     */
    public static Ticket getTicketGrantingTicket(final RequestContext context) {
        return context.getFlowScope().get("ticketGrantingTicket", Ticket.class);
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticket  the ticket value
     */
    public static void putTicketGrantingTicketInScopes(final RequestContext context, final Ticket ticket) {
        putTicketGrantingTicket(context, ticket);
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
    public static void putTicketGrantingTicketIntoMap(final MutableAttributeMap<Object> map, final String ticketValue) {
        FunctionUtils.doIf(StringUtils.isNotBlank(ticketValue),
                value -> map.put(PARAMETER_TICKET_GRANTING_TICKET_ID, value),
                value -> map.remove(PARAMETER_TICKET_GRANTING_TICKET_ID))
            .accept(ticketValue);
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
    public static void putServiceTicketInRequestScope(final RequestContext context, final Ticket ticketValue) {
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
    public static void putLogoutRequests(final RequestContext context, final List<SingleLogoutRequestContext> requests) {
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
    public static List<SingleLogoutRequestContext> getLogoutRequests(final RequestContext context) {
        return (List<SingleLogoutRequestContext>) context.getFlowScope().get(PARAMETER_LOGOUT_REQUESTS);
    }

    /**
     * Put service into flowscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putServiceIntoFlowScope(final RequestContext context, final Service service) {
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
    }

    /**
     * Put service into flashscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putServiceIntoFlashScope(final RequestContext context, final Service service) {
        context.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_SERVICE, service);
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
     * Put registered service.
     *
     * @param request           the request
     * @param registeredService the registered service
     */
    public static void putRegisteredService(final HttpServletRequest request, final RegisteredService registeredService) {
        request.setAttribute(CasWebflowConstants.ATTRIBUTE_REGISTERED_SERVICE, registeredService);
    }

    /**
     * Put registered service into flowscope.
     *
     * @param context           the context
     * @param registeredService the service
     */
    public static void putRegisteredService(final RequestContext context, final RegisteredService registeredService) {
        context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_REGISTERED_SERVICE, registeredService);
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
     * @return the credential, or null if it can't be found in the context or if it has no id.
     */
    public static Credential getCredential(final RequestContext context) {
        val cFromRequest = (Credential) context.getRequestScope().get(PARAMETER_CREDENTIAL);
        val cFromFlashScope = (Credential) context.getFlashScope().get(PARAMETER_CREDENTIAL);
        val cFromFlow = (Credential) context.getFlowScope().get(PARAMETER_CREDENTIAL);
        val cFromConversation = (Credential) context.getConversationScope().get(PARAMETER_CREDENTIAL);

        var credential = cFromRequest;
        if (credential == null || StringUtils.isBlank(credential.getId())) {
            credential = cFromFlow;
        }
        if (credential == null || StringUtils.isBlank(credential.getId())) {
            credential = cFromFlashScope;
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
     * @param context    the context
     * @param credential the credential
     */
    public static void putCredential(final RequestContext context, final Credential credential) {
        if (credential == null) {
            context.getRequestScope().remove(PARAMETER_CREDENTIAL);
            context.getFlowScope().remove(PARAMETER_CREDENTIAL);
            context.getConversationScope().remove(PARAMETER_CREDENTIAL);
        } else {
            putCredentialIntoScope(context.getRequestScope(), credential);
            putCredentialIntoScope(context.getFlowScope(), credential);
            putCredentialIntoScope(context.getConversationScope(), credential);
        }
    }

    /**
     * Put credential into scope.
     *
     * @param scope      the scope
     * @param credential the credential
     */
    public static void putCredentialIntoScope(final MutableAttributeMap<Object> scope, final Credential credential) {
        scope.put(PARAMETER_CREDENTIAL, credential);
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
        val foundParameter = ctx.getFlowScope().contains(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION);
        if (foundParameter && BooleanUtils.toBoolean(ctx.getFlowScope().getBoolean(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION))) {
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
        val foundParameter = context.getRequestParameters().contains(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION);
        if (foundParameter && context.getRequestParameters().getBoolean(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION)) {
            context.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_PUBLIC_WORKSTATION, Boolean.TRUE);
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
            val foundParameter = context.getRequestParameters().contains(CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT);
            if (foundParameter && context.getRequestParameters().getBoolean(CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT)) {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
                warnCookieGenerator.addCookie(request, response, "true");
            }
        } else {
            LOGGER.trace("No warning cookie generator is defined");
        }
    }

    /**
     * Put authentication into conversation scope.
     *
     * @param authentication the authentication
     * @param requestContext the ctx
     */
    public static void putAuthentication(final Authentication authentication, final RequestContext requestContext) {
        putAuthentication(authentication, requestContext.getConversationScope());
        putAuthentication(authentication, requestContext.getFlowScope());
    }

    /**
     * Put authentication.
     *
     * @param ticket         the ticket
     * @param requestContext the request context
     */
    public static void putAuthentication(final Ticket ticket, final RequestContext requestContext) {
        if (ticket instanceof final AuthenticationAwareTicket aat) {
            putAuthentication(aat.getAuthentication(), requestContext);
        }
    }

    /**
     * Put authentication.
     *
     * @param authentication the authentication
     * @param scope          the scope
     */
    public static void putAuthentication(final Authentication authentication, final MutableAttributeMap scope) {
        scope.put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION, authentication);
    }

    /**
     * Gets authentication from conversation scope.
     *
     * @param ctx the ctx
     * @return the authentication
     */
    public static Authentication getAuthentication(final RequestContext ctx) {
        return getAuthentication(ctx.getConversationScope());
    }

    /**
     * Gets authentication.
     *
     * @param ctx the ctx
     * @return the authentication
     */
    public static Authentication getAuthentication(final MutableAttributeMap<Object> ctx) {
        return ctx.get(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION, Authentication.class);
    }

    /**
     * Put authentication result builder.
     *
     * @param builder the builder
     * @param ctx     the ctx
     */
    public static void putAuthenticationResultBuilder(final AuthenticationResultBuilder builder, final RequestContext ctx) {
        ctx.getConversationScope().put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER, builder);
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
        return ctx.getConversationScope().get(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT_BUILDER, AuthenticationResultBuilder.class);
    }

    /**
     * Put authentication result.
     *
     * @param authenticationResult the authentication result
     * @param context              the context
     */
    public static void putAuthenticationResult(final AuthenticationResult authenticationResult, final RequestContext context) {
        context.getConversationScope().put(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT, authenticationResult);
    }

    /**
     * Gets authentication result builder.
     *
     * @param ctx the ctx
     * @return the authentication context builder
     */
    public static AuthenticationResult getAuthenticationResult(final RequestContext ctx) {
        return ctx.getConversationScope().get(CasWebflowConstants.ATTRIBUTE_AUTHENTICATION_RESULT, AuthenticationResult.class);
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
     * Gets geo location tracking into flow scope.
     *
     * @param context the context
     * @return the geo location tracking into flow scope
     */
    public static Boolean isGeoLocationTrackingIntoFlowScope(final RequestContext context) {
        return context.getFlowScope().get("trackGeoLocation", Boolean.class);
    }

    /**
     * Put recaptcha settings flow scope.
     *
     * @param context         the context
     * @param googleRecaptcha the properties
     */
    public static void putRecaptchaPropertiesFlowScope(final RequestContext context, final GoogleRecaptchaProperties googleRecaptcha) {
        val flowScope = context.getFlowScope();
        if (googleRecaptcha.isEnabled()) {
            flowScope.put("recaptchaSiteKey", SpringExpressionLanguageValueResolver.getInstance().resolve(googleRecaptcha.getSiteKey()));
            flowScope.put("recaptchaInvisible", googleRecaptcha.isInvisible());
            flowScope.put("recaptchaPosition", googleRecaptcha.getPosition());
            flowScope.put("recaptchaVersion", googleRecaptcha.getVersion().name().toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Gets recaptcha site key.
     *
     * @param context the context
     * @return the recaptcha site key
     */
    public static String getRecaptchaSiteKey(final RequestContext context) {
        val flowScope = context.getFlowScope();
        return SpringExpressionLanguageValueResolver.getInstance().resolve(flowScope.get("recaptchaSiteKey", String.class));
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
     * Put password management enabled flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putPasswordManagementEnabled(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("passwordManagementEnabled", value);
    }

    /**
     * Put forgot username enabled into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putForgotUsernameEnabled(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("forgotUsernameEnabled", value);
    }

    /**
     * Put account profile management enabled.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putAccountProfileManagementEnabled(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("accountProfileManagementEnabled", value);
    }

    /**
     * Put security questions enabled into flow scope.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putSecurityQuestionsEnabled(final RequestContext context, final Boolean value) {
        context.getFlowScope().put("securityQuestionsEnabled", value);
    }

    /**
     * Is password management enabled.
     *
     * @param context the context
     * @return true/false
     */
    public static boolean isPasswordManagementEnabled(final RequestContext context) {
        return context.getFlowScope().get("passwordManagementEnabled", Boolean.class);
    }

    /**
     * Is forgot username enabled.
     *
     * @param context the context
     * @return true/false
     */
    public static boolean isForgotUsernameEnabled(final RequestContext context) {
        return context.getFlowScope().get("forgotUsernameEnabled", Boolean.class);
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
     * Put logout redirect url.
     *
     * @param request the context
     * @param service the service
     */
    public static void putLogoutRedirectUrl(final HttpServletRequest request, final String service) {
        request.setAttribute("logoutRedirectUrl", service);
    }

    /**
     * Gets logout redirect url.
     *
     * @param <T>     the type parameter
     * @param request the request
     * @param clazz   the clazz
     * @return the logout redirect url
     */
    public static <T> T getLogoutRedirectUrl(final HttpServletRequest request, final Class<T> clazz) {
        val value = request.getAttribute("logoutRedirectUrl");
        return Optional.ofNullable(value).map(clazz::cast).orElse(null);
    }

    /**
     * Gets logout redirect url.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the logout redirect url
     */
    public static <T> T getLogoutRedirectUrl(final RequestContext context, final Class<T> clazz) {
        return context.getFlowScope().get("logoutRedirectUrl", clazz);
    }

    /**
     * Remove logout redirect url.
     *
     * @param context the context
     */
    public static void removeLogoutRedirectUrl(final RequestContext context) {
        context.getFlowScope().remove("logoutRedirectUrl");
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
     * @return true /false
     */
    public static Boolean isRememberMeAuthenticationEnabled(final RequestContext context) {
        return context.getFlowScope().getBoolean("rememberMeAuthenticationEnabled", Boolean.FALSE);
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
     * Gets service redirect url.
     *
     * @param requestContext the request context
     * @return the service redirect url
     */
    public static String getServiceRedirectUrl(final RequestContext requestContext) {
        return requestContext.getRequestScope().get("url", String.class);
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
        requestContext.getRequestScope().put("parameters", response.attributes());
        putServiceRedirectUrl(requestContext, response.url());
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
     * @param ex the ex
     * @return the model and view
     */
    public static ModelAndView produceUnauthorizedErrorView(final Exception ex) {
        val error = UnauthorizedServiceException.wrap(ex);
        return produceErrorView(error);
    }

    /**
     * Produce error view model and view.
     *
     * @param view the view
     * @param e    the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final String view, final Throwable e) {
        val rootCause = (e instanceof final RuntimeException er && er.getCause() != null)
            ? ExceptionUtils.getRootCause(e)
            : e;
        val mv = new ModelAndView(view, CollectionUtils.wrap(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, rootCause));
        mv.setStatus(HttpStatus.BAD_REQUEST);
        LoggingUtils.error(LOGGER, e);
        return mv;
    }

    /**
     * Produce error view.
     *
     * @param request the request
     * @param status  the bad request
     * @param message the message
     */
    public static void produceErrorView(final HttpServletRequest request, final HttpStatus status, final String message) {
        request.setAttribute("status", status.value());
        request.setAttribute("error", status.name());
        request.setAttribute("message", message);
    }

    /**
     * Produce error view model and view.
     *
     * @param e the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Throwable e) {
        return produceErrorView(CasWebflowConstants.VIEW_ID_SERVICE_ERROR, e);
    }

    /**
     * Gets in progress authentication.
     *
     * @return the in progress authentication
     */
    public static Authentication getInProgressAuthentication() {
        val context = RequestContextHolder.getRequestContext();
        return Optional.ofNullable(context).map(WebUtils::getAuthentication).orElseGet(AuthenticationHolder::getCurrentAuthentication);
    }

    /**
     * Put request surrogate authentication.
     *
     * @param context the context
     * @param value   the value
     */
    public static void putSurrogateAuthenticationRequest(final RequestContext context, final Boolean value) {
        context.getFlowScope().put(REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE, value);
    }

    /**
     * Has request surrogate authentication request.
     *
     * @param requestContext the request context
     * @return true /false
     */
    public static boolean hasSurrogateAuthenticationRequest(final RequestContext requestContext) {
        return BooleanUtils.toBoolean(requestContext.getFlowScope().getBoolean(REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE, Boolean.FALSE));
    }

    /**
     * Has request surrogate authentication request.
     *
     * @param requestContext the request context
     */
    public static void removeSurrogateAuthenticationRequest(final RequestContext requestContext) {
        requestContext.getFlowScope().remove(REQUEST_SURROGATE_ACCOUNT_ATTRIBUTE);
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
     * @return true /false
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
     * @return true /false
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
     * @return true /false
     */
    public static boolean containsGraphicalUserAuthenticationImage(final RequestContext requestContext) {
        return requestContext.getFlowScope().contains("guaUserImage");
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
     * Gets available authentication handle names.
     *
     * @param context the context
     * @return the available authentication handle names
     */
    public static Collection<String> getAvailableAuthenticationHandleNames(final RequestContext context) {
        return context.getFlowScope().get("availableAuthenticationHandlerNames", Collection.class);
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
            return requestContext.getFlowScope().get("aupPolicy", clazz);
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
        val queryParams = StringUtils.defaultString(request.getQueryString());
        val parameters = request.getParameterMap()
            .entrySet()
            .stream()
            .filter(param -> !queryParams.contains(param.getKey() + '='))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        context.getFlashScope().put("httpRequestInitialPostParameters", parameters);
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
     * Is existing single sign on session available boolean.
     *
     * @param context the context
     * @return true/false
     */
    public static Boolean isExistingSingleSignOnSessionAvailable(final MockRequestContext context) {
        return context.getFlowScope().get("existingSingleSignOnSessionAvailable", Boolean.class);
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
     * @return true /false Defaults to TRUE if not set in flow scope
     */
    public static boolean isCasLoginFormViewable(final RequestContext context) {
        return context.getFlowScope().getBoolean("casLoginFormViewable", Boolean.TRUE);
    }

    /**
     * Has login form been set to viewable explicitly.
     *
     * @param context the context
     * @return true /false Defaults to FALSE if not set in flow scope
     */
    public static boolean isCasLoginFormSetToViewable(final RequestContext context) {
        return context.getFlowScope().getBoolean("casLoginFormViewable", Boolean.FALSE);
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
    @SuppressWarnings("JdkObsolete")
    public static String getHttpRequestFullUrl(final HttpServletRequest request) {
        val requestURL = request.getRequestURL();
        val queryString = request.getQueryString();
        return Optional.ofNullable(queryString).map(query -> requestURL.append('?').append(query).toString()).orElseGet(requestURL::toString);
    }

    /**
     * Create credential.
     *
     * @param requestContext the request context
     */
    public static void createCredential(final RequestContext requestContext) {
        removeCredential(requestContext);
        val flow = (Flow) requestContext.getActiveFlow();
        val flowVariable = flow.getVariable(CasWebflowConstants.VAR_ID_CREDENTIAL);
        if (flowVariable != null) {
            flowVariable.create(requestContext);
        }
    }

    /**
     * Put single logout request.
     *
     * @param request       the request
     * @param logoutRequest the logout request
     */
    public static void putSingleLogoutRequest(final HttpServletRequest request, final String logoutRequest) {
        request.setAttribute("singleLogoutRequest", logoutRequest);
    }

    /**
     * Gets single logout request.
     *
     * @param request the request
     * @return the single logout request
     */
    public static String getSingleLogoutRequest(final HttpServletRequest request) {
        return (String) request.getAttribute("singleLogoutRequest");
    }


    /**
     * Put authorized services.
     *
     * @param requestContext     the request context
     * @param authorizedServices the authorized services
     */
    public static void putAuthorizedServices(final RequestContext requestContext, final List<RegisteredService> authorizedServices) {
        requestContext.getFlowScope().put("authorizedServices", authorizedServices);
    }

    /**
     * Put single sign on sessions.
     *
     * @param requestContext the request context
     * @param sessions       the sessions
     */
    public static void putSingleSignOnSessions(final RequestContext requestContext, final List<? extends Serializable> sessions) {
        requestContext.getFlowScope().put("singleSignOnSessions", sessions);
    }

    /**
     * Gets single sign on sessions.
     *
     * @param requestContext the request context
     * @return the single sign on sessions
     */
    public static List<? extends Serializable> getSingleSignOnSessions(final RequestContext requestContext) {
        return (List<? extends Serializable>) requestContext.getFlowScope().get("singleSignOnSessions", List.class);
    }

    /**
     * Gets authorized services.
     *
     * @param requestContext the request context
     * @return the authorized services
     */
    public List<RegisteredService> getAuthorizedServices(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("authorizedServices", List.class);
    }

    /**
     * Put recaptcha forgot username enabled.
     *
     * @param requestContext the request context
     * @param properties     the properties
     */
    public static void putRecaptchaForgotUsernameEnabled(final RequestContext requestContext, final GoogleRecaptchaProperties properties) {
        requestContext.getFlowScope().put("recaptchaForgotUsernameEnabled", properties.isEnabled());
    }

    /**
     * Is recaptcha forgot username enabled.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static Boolean isRecaptchaForgotUsernameEnabled(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("recaptchaForgotUsernameEnabled", Boolean.class);
    }

    /**
     * Put recaptcha password management enabled.
     *
     * @param requestContext the request context
     * @param recaptcha      the recaptcha
     */
    public static void putRecaptchaPasswordManagementEnabled(final RequestContext requestContext, final GoogleRecaptchaProperties recaptcha) {
        requestContext.getFlowScope().put("recaptchaPasswordManagementEnabled", recaptcha.isEnabled());
    }

    /**
     * Is recaptcha forgot username enabled.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static Boolean isRecaptchaPasswordManagementEnabled(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("recaptchaPasswordManagementEnabled", Boolean.class);
    }


    /**
     * Resolve registered service.
     *
     * @param requestContext           the request context
     * @param servicesManager          the services manager
     * @param serviceSelectionStrategy the service selection strategy
     * @return the service
     */
    public static RegisteredService resolveRegisteredService(final RequestContext requestContext,
                                                             final ServicesManager servicesManager,
                                                             final AuthenticationServiceSelectionPlan serviceSelectionStrategy) throws Throwable {
        val registeredService = getRegisteredService(requestContext);
        if (registeredService != null) {
            return registeredService;
        }
        val service = WebUtils.getService(requestContext);
        val serviceToUse = serviceSelectionStrategy.resolveService(service);
        if (serviceToUse != null) {
            return servicesManager.findServiceBy(serviceToUse);
        }
        return null;
    }

    /**
     * Add error message to context.
     *
     * @param requestContext the request context
     * @param code           the code
     * @param defaultText    the default text
     * @param args           the args
     */
    public static void addErrorMessageToContext(final RequestContext requestContext, final String code,
                                                final String defaultText, final Object[] args) {
        addErrorMessageToContext(requestContext.getMessageContext(), code, defaultText, args);
    }

    /**
     * Add error message to context.
     *
     * @param requestContext the request context
     * @param code           the code
     * @param defaultText    the default text
     */
    public static void addErrorMessageToContext(final RequestContext requestContext, final String code,
                                                final String defaultText) {
        addErrorMessageToContext(requestContext.getMessageContext(), code, defaultText, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Add error message to context.
     *
     * @param requestContext the request context
     * @param code           the code
     */
    public static void addErrorMessageToContext(final RequestContext requestContext, final String code) {
        addErrorMessageToContext(requestContext.getMessageContext(), code, null, null);
    }

    /**
     * Add error message to context.
     *
     * @param messageContext the message context
     * @param code           the code
     * @param defaultText    the default text
     * @param args           the args
     */
    public static void addErrorMessageToContext(final MessageContext messageContext, final String code,
                                                final String defaultText, final Object[] args) {
        val msg = new MessageBuilder()
            .error()
            .code(code)
            .args(args)
            .defaultText(defaultText)
            .build();
        messageContext.addMessage(msg);
    }

    /**
     * Add info message to context.
     *
     * @param requestContext the request context
     * @param code           the code
     */
    public static void addInfoMessageToContext(final RequestContext requestContext, final String code) {
        val msg = new MessageBuilder()
            .info()
            .code(code)
            .build();
        requestContext.getMessageContext().addMessage(msg);
    }

    /**
     * Put the logout POST url in the flow scope.
     *
     * @param requestContext the flow context
     * @param postUrl        the POST url
     */
    public static void putLogoutPostUrl(final RequestContext requestContext, final String postUrl) {
        requestContext.getFlowScope().put("logoutPostUrl", postUrl);
    }

    /**
     * Put the logout POST data in the flow scope.
     *
     * @param requestContext the flow context
     * @param postData       the POST data
     */
    public static void putLogoutPostData(final RequestContext requestContext, final Map<String, Object> postData) {
        requestContext.getFlowScope().put("logoutPostData", postData);
    }

    /**
     * Get the logout POST url from the flow scope.
     *
     * @param requestContext the flow context
     * @return the POST url
     */
    public static String getLogoutPostUrl(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("logoutPostUrl", String.class);
    }

    /**
     * Get the logout POST data from the flow scope.
     *
     * @param requestContext the flow context
     * @return the POST data
     */
    public static Map<String, Object> getLogoutPostData(final RequestContext requestContext) {
        return (Map<String, Object>) requestContext.getFlowScope().get("logoutPostData", Map.class);
    }

    /**
     * Put password reset password policy pattern string.
     *
     * @param requestContext the request context
     * @param policyPattern  the policy pattern
     */
    public static void putPasswordPolicyPattern(final RequestContext requestContext, final String policyPattern) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put("passwordPolicyPattern", policyPattern);
    }

    /**
     * Gets password reset password policy pattern.
     *
     * @param requestContext the request context
     * @return the password reset password policy pattern
     */
    public static String getPasswordPolicyPattern(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        return flowScope.get("passwordPolicyPattern", String.class);
    }

    /**
     * Is interrupt authentication flow finalized.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isInterruptAuthenticationFlowFinalized(final RequestContext requestContext) {
        return requestContext.getRequestScope().contains("authenticationFlowInterruptFinalized");
    }

    /**
     * Put interrupt authentication flow finalized.
     *
     * @param requestContext the request context
     */
    public static void putInterruptAuthenticationFlowFinalized(final RequestContext requestContext) {
        requestContext.getRequestScope().put("authenticationFlowInterruptFinalized", Boolean.TRUE);
    }

    /**
     * Put ws federation delegated clients.
     *
     * @param context the context
     * @param clients the clients
     */
    public static void putWsFederationDelegatedClients(final RequestContext context, final List<? extends Serializable> clients) {
        context.getFlowScope().put("wsfedUrls", clients);
    }

    /**
     * Get ws federation delegated clients as list.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param clazz   the clazz
     * @return the list
     */
    public static <T extends Serializable> List<T> getWsFederationDelegatedClients(final RequestContext context, final Class<T> clazz) {
        return (List<T>) context.getFlowScope().get("wsfedUrls", List.class);
    }

    /**
     * Put wildcarded registered service.
     *
     * @param context the context
     * @param result  the result
     */
    public static void putWildcardedRegisteredService(final RequestContext context, final boolean result) {
        context.getFlowScope().put("wildcardedRegisteredService", result);
    }


    /**
     * Put target state.
     *
     * @param requestContext the request context
     * @param target         the target
     */
    public static void putTargetTransition(final RequestContext requestContext, final String target) {
        requestContext.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_TARGET_TRANSITION, target);
    }

    /**
     * Gets target state.
     *
     * @param requestContext the request context
     * @return the target state
     */
    public static String getTargetTransition(final RequestContext requestContext) {
        return requestContext.getFlashScope().get(CasWebflowConstants.ATTRIBUTE_TARGET_TRANSITION, String.class);
    }

    /**
     * Put password management query.
     *
     * @param requestContext the request context
     * @param query          the query
     */
    public static void putPasswordManagementQuery(final RequestContext requestContext, final Serializable query) {
        requestContext.getFlowScope().put(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_QUERY, query);
    }

    /**
     * Gets password management query.
     *
     * @param <T>            the type parameter
     * @param requestContext the request context
     * @param clazz          the clazz
     * @return the password management query
     */
    public static <T> T getPasswordManagementQuery(final RequestContext requestContext, final Class<T> clazz) {
        return requestContext.getFlowScope().get(CasWebflowConstants.ATTRIBUTE_PASSWORD_MANAGEMENT_QUERY, clazz);
    }

    /**
     * Put active flow id.
     *
     * @param requestContext the request context
     */
    public static void putActiveFlow(final RequestContext requestContext) {
        val id = requestContext.getActiveFlow().getId();
        requestContext.getFlashScope().put("activeFlowId", id);
        requestContext.getFlowScope().put("activeFlowId", id);
        requestContext.getConversationScope().put("activeFlowId", id);
    }

    /**
     * Gets active flow.
     *
     * @param requestContext the request context
     * @return the active flow
     */
    public static String getActiveFlow(final RequestContext requestContext) {
        return (String) requestContext.getFlashScope().get("activeFlowId",
            requestContext.getFlowScope().get("activeFlowId",
                requestContext.getConversationScope().get("activeFlowId")));
    }

    /**
     * Gets browser storage context key.
     *
     * @param requestContext the request context
     * @return the browser storage context key
     */
    public String getBrowserStorageContextKey(final RequestContext requestContext, final String defaultKey) {
        return requestContext.getFlowScope().get("browserStorageContextKey", String.class, defaultKey);
    }

    /**
     * Put browser storage context key.
     *
     * @param requestContext           the request context
     * @param browserStorageContextKey the browser storage context key
     */
    public static void putBrowserStorageContextKey(final RequestContext requestContext, final String browserStorageContextKey) {
        requestContext.getFlowScope().put("browserStorageContextKey", browserStorageContextKey);
    }

    /**
     * Read browser storage from request.
     *
     * @param requestContext the request context
     * @return the optional
     * @throws Exception the exception
     */
    public static Optional<String> getBrowserStoragePayload(final RequestContext requestContext) throws Exception {
        if (requestContext.getRequestParameters().contains(BrowserStorage.PARAMETER_BROWSER_STORAGE)) {
            return Optional.of(requestContext.getRequestParameters().getRequired(BrowserStorage.PARAMETER_BROWSER_STORAGE))
                .stream()
                .filter(StringUtils::isNotBlank)
                .findFirst();
        }
        val httpServletRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return getBrowserStoragePayload(httpServletRequest);
    }

    /**
     * Gets browser storage.
     *
     * @param httpServletRequest the http servlet request
     * @return the browser storage
     */
    public static Optional<String> getBrowserStoragePayload(final HttpServletRequest httpServletRequest) {
        val parameters = getHttpRequestParametersFromRequestBody(httpServletRequest);
        FunctionUtils.doIfNotBlank(httpServletRequest.getParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE),
            value -> parameters.put(BrowserStorage.PARAMETER_BROWSER_STORAGE, value));
        return parameters
            .entrySet()
            .stream()
            .filter(param -> param.getKey().equalsIgnoreCase(BrowserStorage.PARAMETER_BROWSER_STORAGE))
            .map(Map.Entry::getValue)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .or(() -> Optional.ofNullable((String) httpServletRequest.getAttribute(BrowserStorage.PARAMETER_BROWSER_STORAGE)));
    }

    /**
     * Put browser storage request.
     *
     * @param requestContext the request context
     * @param browserStorage the browser storage
     */
    public static void putBrowserStorage(final RequestContext requestContext, final BrowserStorage browserStorage) {
        requestContext.getFlowScope().put(BrowserStorage.PARAMETER_BROWSER_STORAGE, browserStorage);
    }

    /**
     * Gets browser storage.
     *
     * @param requestContext the request context
     * @return the browser storage
     */
    public static BrowserStorage getBrowserStorage(final RequestContext requestContext) {
        return requestContext.getFlowScope().get(BrowserStorage.PARAMETER_BROWSER_STORAGE, BrowserStorage.class);
    }

    /**
     * Put target state.
     *
     * @param requestContext the request context
     * @param id             the id
     */
    public static void putTargetState(final RequestContext requestContext, final String id) {
        requestContext.getFlowScope().put("targetState", id);
    }

    /**
     * Gets target state.
     *
     * @param requestContext the request context
     * @return the target state
     */
    public static String getTargetState(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("targetState", String.class);
    }

    /**
     * Gets http request parameters.
     *
     * @param httpServletRequest the http servlet request
     * @return the http request parameters from request body
     */
    public static Map<String, String> getHttpRequestParametersFromRequestBody(final HttpServletRequest httpServletRequest) {
        if (HttpMethod.POST.matches(httpServletRequest.getMethod())
            && StringUtils.equalsIgnoreCase(httpServletRequest.getContentType(), MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            try (val is = httpServletRequest.getInputStream()) {
                if (!is.isFinished()) {
                    val requestBody = IOUtils.toString(is, StandardCharsets.UTF_8);
                    val encodedParams = WWWFormCodec.parse(requestBody, StandardCharsets.UTF_8);
                    return encodedParams
                        .stream()
                        .filter(param -> StringUtils.isNotBlank(param.getValue()))
                        .peek(param -> httpServletRequest.setAttribute(param.getName(), param.getValue()))
                        .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return new HashMap<>();
    }

    /**
     * Gets request parameter or attribute.
     *
     * @param requestContext the request context
     * @param name           the name
     * @return the request parameter or attribute
     */
    public Optional<String> getRequestParameterOrAttribute(final RequestContext requestContext, final String name) {
        val httpServletRequest = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return getRequestParameterOrAttribute(httpServletRequest, name);
    }

    /**
     * Gets request parameter or attribute.
     *
     * @param request the request
     * @param name    the name
     * @return the request parameter or attribute
     */
    public Optional<String> getRequestParameterOrAttribute(final HttpServletRequest request, final String name) {
        return Optional.ofNullable(request.getParameter(name))
            .or(() -> Optional.ofNullable((String) request.getAttribute(name)))
            .filter(StringUtils::isNotBlank);
    }

    /**
     * Track failed authentication attempts.
     *
     * @param requestContext the request context
     */
    public static void trackFailedAuthenticationAttempt(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        val attempts = flowScope.contains("authenticationFailureCount", Integer.class)
            ? flowScope.get("authenticationFailureCount", Integer.class)
            : 0;
        flowScope.put("authenticationFailureCount", attempts + 1);
    }

    /**
     * Gets authentication failure count.
     *
     * @param requestContext the request context
     * @return the authentication failure count
     */
    public static Integer countFailedAuthenticationAttempts(final RequestContext requestContext) {
        return requestContext.getFlowScope().get("authenticationFailureCount", Integer.class);
    }

    /**
     * To model and view.
     *
     * @param status   the status
     * @param viewName the view name
     * @return the model and view
     */
    public static ModelAndView toModelAndView(final HttpStatus status, final String viewName) {
        val mv = new ModelAndView();
        mv.setStatus(HttpStatusCode.valueOf(status.value()));
        mv.setViewName(viewName);
        return mv;
    }

    /**
     * Add warning message to context.
     *
     * @param context the context
     * @param warning the warning
     */
    public static void addWarningMessageToContext(final MessageContext context, final MessageDescriptor warning) {
        val builder = new MessageBuilder()
            .warning()
            .code(warning.getCode())
            .defaultText(warning.getDefaultMessage())
            .args((Object[]) warning.getParams());
        context.addMessage(builder.build());
    }

    /**
     * Is logout request confirmed?.
     *
     * @param requestContext the request context
     * @return true/false
     */
    public static boolean isLogoutRequestConfirmed(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameterMap().containsKey(REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED)
            && Boolean.parseBoolean(request.getParameter(REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED));
    }
}
