package org.jasig.cas.web.support;

import org.jasig.cas.authentication.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.http.HttpRequestGeoLocation;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

/**
 * Common utilities for the web tier.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class WebUtils {

    /** Default CAS Servlet name. **/
    public static final String CAS_SERVLET_NAME = "cas";

    /** Request attribute that contains message key describing details of authorization failure.*/
    public static final String CAS_ACCESS_DENIED_REASON = "CAS_ACCESS_DENIED_REASON";

    /** Constant representing the request header for user agent. */
    public static final String USER_AGENT_HEADER = "user-agent";

    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);

    private static final String UNKNOWN_USER = "audit:unknown";

    /** Flow scope attribute that determines if authn is happening at a public workstation. */
    private static final String PUBLIC_WORKSTATION_ATTRIBUTE = "publicWorkstation";

    /**
     * Instantiates a new web utils instance.
     */
    private WebUtils() {}

    /**
     * Gets the http servlet request from the context.
     *
     * @param context the context
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
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
        } else {
            return null;
        }
    }

    /**
     * Gets the http servlet response from the context.
     *
     * @param context the context
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponse(
            final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
                        .getExternalContext(),
                "Cannot obtain HttpServletResponse from event of type: "
                        + context.getExternalContext().getClass().getName());
        return (HttpServletResponse) context.getExternalContext()
                .getNativeResponse();
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
        } else {
            return null;
        }
    }

    /**
     * Is cas servlet initializing?
     *
     * @param sce the sce
     * @return the boolean
     */
    public static boolean isCasServletInitializing(final ServletContext sce) {
        return sce.getServletRegistrations().containsKey(CAS_SERVLET_NAME);
    }

    /**
     * Is cas servlet initializing?
     *
     * @param sce the sce
     * @return the boolean
     */
    public static boolean isCasServletInitializing(final ServletContextEvent sce) {
        return sce.getServletContext().getServletRegistrations().containsKey(CAS_SERVLET_NAME);
    }

    /**
     * Is cas servlet initializing?
     *
     * @param sce the sce
     * @return the boolean
     */
    public static boolean isCasServletInitializing(final WebApplicationContext sce) {
        return isCasServletInitializing(sce.getServletContext());
    }

    /**
     * Is cas servlet initializing.
     *
     * @param sce the sce
     * @return the boolean
     */
    public static boolean isCasServletInitializing(final ApplicationContext sce) {
        if (sce instanceof WebApplicationContext) {
            return isCasServletInitializing(((WebApplicationContext) sce).getServletContext());
        }
        LOGGER.debug("No CAS servlet is available because the given application context is not of type {}",
                WebApplicationContext.class);
        return false;
    }

    /**
     * Gets the service from the request based on given extractors.
     *
     * @param argumentExtractors the argument extractors
     * @param request the request
     * @return the service, or null.
     */
    public static WebApplicationService getService(
        final List<ArgumentExtractor> argumentExtractors,
        final HttpServletRequest request) {
        return argumentExtractors.stream().map(argumentExtractor -> argumentExtractor.extractService(request))
                .filter(service -> service != null).findFirst().orElse(null);
    }

    /**
     * Gets the service.
     *
     * @param argumentExtractors the argument extractors
     * @param context the context
     * @return the service
     */
    public static WebApplicationService getService(
        final List<ArgumentExtractor> argumentExtractors,
        final RequestContext context) {
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
        return context != null ? (WebApplicationService) context.getFlowScope().get("service") : null;
    }

    /**
     * Gets the registered service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static RegisteredService getRegisteredService(final RequestContext context) {
        return context != null ? (RegisteredService) context.getFlowScope().get("registeredService") : null;
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticket the ticket value
     */
    public static void putTicketGrantingTicketInScopes(
        final RequestContext context, @NotNull final TicketGrantingTicket ticket) {
        final String ticketValue = ticket != null ? ticket.getId() : null;
        putTicketGrantingTicketInScopes(context, ticketValue);
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketInScopes(
            final RequestContext context, @NotNull final String ticketValue) {
        putTicketGrantingTicketIntoMap(context.getRequestScope(), ticketValue);
        putTicketGrantingTicketIntoMap(context.getFlowScope(), ticketValue);
    }

    /**
     * Put ticket granting ticket into map that is either backed by the flow/request scope.
     * Will override the previous value and blank out the setting if value is null or empty.
     * @param map the map
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketIntoMap(final MutableAttributeMap map,
                                                       @NotNull final String ticketValue) {
        map.put("ticketGrantingTicketId", ticketValue);
    }

    /**
     * Gets the ticket granting ticket id from the request and flow scopes.
     *
     * @param context the context
     * @return the ticket granting ticket id
     */
    public static String getTicketGrantingTicketId(
            @NotNull final RequestContext context) {
        final String tgtFromRequest = (String) context.getRequestScope().get("ticketGrantingTicketId");
        final String tgtFromFlow = (String) context.getFlowScope().get("ticketGrantingTicketId");

        return tgtFromRequest != null ? tgtFromRequest : tgtFromFlow;

    }

    /**
     * Put service ticket in request scope.
     *
     * @param context the context
     * @param ticketValue the ticket value
     */
    public static void putServiceTicketInRequestScope(
        final RequestContext context, final ServiceTicket ticketValue) {
        context.getRequestScope().put("serviceTicketId", ticketValue.getId());
    }

    /**
     * Gets the service ticket from request scope.
     *
     * @param context the context
     * @return the service ticket from request scope
     */
    public static String getServiceTicketFromRequestScope(
        final RequestContext context) {
        return context.getRequestScope().getString("serviceTicketId");
    }

    /**
     * Adds the unauthorized redirect url to the flow scope.
     * @param context the request context
     * @param url the uri to redirect the flow
     */
    public static void putUnauthorizedRedirectUrlIntoFlowScope(final RequestContext context, final URI url) {
        context.getFlowScope().put("unauthorizedRedirectUrl", url);
    }

    /**
     * Put logout requests into flow scope.
     *
     * @param context the context
     * @param requests the requests
     */
    public static void putLogoutRequests(final RequestContext context, final List<LogoutRequest> requests) {
        context.getFlowScope().put("logoutRequests", requests);
    }

    /**
     * Gets the logout requests from flow scope.
     *
     * @param context the context
     * @return the logout requests
     */
    public static List<LogoutRequest> getLogoutRequests(final RequestContext context) {
        return (List<LogoutRequest>) context.getFlowScope().get("logoutRequests");
    }

    /**
     * Put service into flowscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putService(final RequestContext context, final Service service) {
        context.getFlowScope().put("service", service);
    }

    /**
     * Put warning cookie value into flowscope.
     *
     * @param context the context
     * @param cookieValue the cookie value
     */
    public static void putWarningCookie(final RequestContext context, final Boolean cookieValue) {
        context.getFlowScope().put("warnCookieValue", cookieValue);
    }

    /**
     * Put registered service into flowscope.
     *
     * @param context the context
     * @param registeredService the service
     */
    public static void putRegisteredService(final RequestContext context,
                                            final RegisteredService registeredService) {
        context.getFlowScope().put("registeredService", registeredService);
    }

    /**
     * Gets credential from the context.
     *
     * @param context the context
     * @return the credential, or null if it cant be found in the context or if it has no id.
     */
    public static Credential getCredential(@NotNull final RequestContext context) {
        final Credential cFromRequest = (Credential) context.getRequestScope().get("credential");
        final Credential cFromFlow = (Credential) context.getFlowScope().get("credential");

        Credential credential = cFromRequest != null ? cFromRequest : cFromFlow;

        if (credential == null) {
            final FlowSession session = context.getFlowExecutionContext().getActiveSession();
            credential = session.getScope().get("credential", Credential.class);
        }
        if (credential != null && StringUtils.isBlank(credential.getId())) {
            return null;
        }
        return credential;
    }

    /**
     * Return the username of the authenticated user (based on pac4j security).
     *
     * @return the authenticated username.
     */
    public static String getAuthenticatedUsername() {
        final HttpServletRequest request = getHttpServletRequest();
        final HttpServletResponse response = getHttpServletResponse();
        if (request != null && response != null) {
            final J2EContext context = new J2EContext(request, response);
            final ProfileManager manager = new ProfileManager(context);
            final UserProfile profile = manager.get(true);
            if (profile != null) {
                final String id = profile.getId();
                if (id != null) {
                    return id;
                }
            }
        }
        return UNKNOWN_USER;
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
            } else {
                warnCookieGenerator.removeCookie(response);
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
        ctx.getConversationScope().put("authentication", authentication);
    }

    /**
     * Gets authentication from conversation scope.
     *
     * @param ctx            the ctx
     * @return the authentication
     */
    public static Authentication getAuthentication(final RequestContext ctx) {
        return ctx.getConversationScope().get("authentication", Authentication.class);
    }

    /**
     * Put authentication result builder.
     *
     * @param builder the builder
     * @param ctx     the ctx
     */
    public static void putAuthenticationResultBuilder(final AuthenticationResultBuilder builder, final RequestContext ctx) {
        ctx.getConversationScope().put("authenticationResultBuilder", builder);
    }

    /**
     * Gets authentication result builder.
     *
     * @param ctx the ctx
     * @return the authentication result builder
     */
    public static AuthenticationResultBuilder getAuthenticationResultBuilder(final RequestContext ctx) {
        return ctx.getConversationScope().get("authenticationResultBuilder", AuthenticationResultBuilder.class);
    }

    /**
     * Put authentication result.
     *
     * @param authenticationResult the authentication result
     * @param context               the context
     */
    public static void putAuthenticationResult(final AuthenticationResult authenticationResult, final RequestContext context) {
        context.getConversationScope().put("authenticationResult", authenticationResult);
    }

    /**
     * Gets authentication result builder.
     *
     * @param ctx the ctx
     * @return the authentication context builder
     */
    public static AuthenticationResult getAuthenticationResult(final RequestContext ctx) {
        return ctx.getConversationScope().get("authenticationResult", AuthenticationResult.class);
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
     * @return the http servlet request geo location
     */
    public static HttpRequestGeoLocation getHttpServletRequestGeoLocation() {
        final int latIndex = 0;
        final int longIndex = 1;
        final int accuIndex = 2;
        final int timeIndex = 3;

        final HttpServletRequest request = WebUtils.getHttpServletRequest();
        final HttpRequestGeoLocation loc = new HttpRequestGeoLocation();
        if (request != null) {
            final String geoLocationParam = request.getParameter("geolocation");
            if (StringUtils.isNotBlank(geoLocationParam)) {
                final String[] geoLocation = geoLocationParam.split(",");
                loc.setLatitude(geoLocation[latIndex]);
                loc.setLongitude(geoLocation[longIndex]);
                loc.setAccuracy(geoLocation[accuIndex]);
                loc.setTimestamp(geoLocation[timeIndex]);
            }
        }
        return loc;
    }
}
