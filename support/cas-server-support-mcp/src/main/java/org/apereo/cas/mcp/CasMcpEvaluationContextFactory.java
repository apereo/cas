package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasMcpEvaluationContextFactory}. It builds what MCP tools evaluate CAS policies against, so that every tool
 * resolves the person and the application the same way: the service built from the application URL, the registered
 * service that matches it, the principal resolved from the attribute repositories (no credential is verified) and an
 * authentication for that principal. Tools that call request-aware components also get a view of the live MCP request
 * that carries no parameters, headers, cookies or query string, so nothing the MCP caller sent leaks into the evaluation,
 * apart from client details the tool was given for the login attempt, or a webflow request context built over it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMcpEvaluationContextFactory {
    private static final String GEOLOCATION_PARAMETER = "geolocation";

    private static final String UNKNOWN_CLIENT = "unknown";

    private static final PrincipalFactory PRINCIPAL_FACTORY = PrincipalFactoryUtils.newPrincipalFactory();

    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final PrincipalResolver principalResolver;

    /**
     * Build the evaluation context for a person signing in to an application. The username is handed to the principal
     * resolvers as the current principal, the way an authentication handler hands over the principal it authenticated,
     * so that a deployment without attribute repositories still resolves the person rather than an unknown principal.
     *
     * @param username   the username of the person
     * @param serviceUrl the application URL
     * @return the evaluation context
     */
    public CasMcpEvaluationContext build(final String username, final String serviceUrl) {
        val service = Objects.requireNonNull(serviceFactory.createService(serviceUrl),
            () -> "Unable to build a service for " + serviceUrl);
        val registeredService = servicesManager.findServiceBy(service);
        val credential = new BasicIdentifiableCredential(username);
        val principal = Objects.requireNonNull(FunctionUtils.doUnchecked(() -> principalResolver.resolve(credential,
                Optional.ofNullable(PRINCIPAL_FACTORY.createPrincipal(username)), Optional.empty(), Optional.of(service))),
            () -> "Unable to resolve a principal for " + username);
        val authentication = DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
        return new CasMcpEvaluationContext(service, registeredService, principal, credential, authentication);
    }

    /**
     * The live MCP request, stripped of parameters, headers, cookies and query string, along with its response.
     *
     * @return the request and response
     * @throws IllegalStateException when called outside of an HTTP request
     */
    public CasMcpHttpExchange currentHttpExchange() {
        return currentHttpExchange(CasMcpClient.unknown());
    }

    /**
     * The live MCP request, stripped of everything the MCP caller sent, that carries only the supplied client details:
     * the user agent as the {@code User-Agent} header, the location as the {@code geolocation} parameter and the client
     * IP address as the remote address, which is {@code unknown} when not supplied.
     *
     * @param client the client details of the login attempt being evaluated
     * @return the request and response
     * @throws IllegalStateException when called outside of an HTTP request
     */
    public CasMcpHttpExchange currentHttpExchange(final CasMcpClient client) {
        val attributes = RequestContextHolder.getRequestAttributes() instanceof final ServletRequestAttributes servletAttributes
            ? servletAttributes : null;
        val response = attributes != null ? attributes.getResponse() : null;
        if (attributes == null || response == null) {
            throw new IllegalStateException("This evaluation can only run within an HTTP request");
        }
        return new CasMcpHttpExchange(new ClientHttpServletRequest(attributes.getRequest(), client), response);
    }

    /**
     * Build the client information CAS records for a login attempt from the supplied client details, keeping the server
     * address and tenant of the live MCP request.
     *
     * @param client the client details
     * @return the client information
     */
    public ClientInfo newClientInfo(final CasMcpClient client) {
        val clientInfo = new ClientInfo()
            .setClientIpAddress(client.clientIpAddress())
            .setUserAgent(client.userAgent())
            .setGeoLocation(client.geoLocation());
        val current = ClientInfoHolder.getClientInfo();
        if (current != null) {
            clientInfo.setServerIpAddress(current.getServerIpAddress());
            clientInfo.setTenant(current.getTenant());
        }
        return clientInfo;
    }

    /**
     * Run an evaluation with the client information of the thread set to the supplied client details, so that components
     * that read it see the login attempt rather than the MCP caller, and restore it afterwards.
     *
     * @param <T>    the result type
     * @param client the client details
     * @param action the evaluation
     * @return the result of the evaluation
     */
    public <T> T withClientInfo(final CasMcpClient client, final Supplier<T> action) {
        val previous = ClientInfoHolder.getClientInfo();
        ClientInfoHolder.setClientInfo(newClientInfo(client));
        try {
            return action.get();
        } finally {
            if (previous != null) {
                ClientInfoHolder.setClientInfo(previous);
            } else {
                ClientInfoHolder.clear();
            }
        }
    }

    /**
     * Build a Spring Webflow request context over the stripped live request, holding the service, registered service,
     * credential and authentication the way the login webflow holds them once the person has authenticated.
     *
     * @param context the evaluation context
     * @return the webflow request context
     * @throws IllegalStateException when called outside of an HTTP request
     */
    public RequestContext newWebflowRequestContext(final CasMcpEvaluationContext context) {
        val exchange = currentHttpExchange();
        val requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(exchange.request().getServletContext(),
            exchange.request(), exchange.response()));
        WebUtils.putServiceIntoFlowScope(requestContext, context.service());
        WebUtils.putRegisteredService(requestContext, context.registeredService());
        WebUtils.putCredential(requestContext, context.credential());
        WebUtils.putAuthentication(context.authentication(), requestContext);
        return requestContext;
    }

    /**
     * The person and application a tool evaluates.
     *
     * @param service           the service built from the application URL
     * @param registeredService the registered service that matches the service, if any
     * @param principal         the resolved principal
     * @param credential        the credential carrying the username; it is never verified
     * @param authentication    an authentication for the resolved principal
     */
    public record CasMcpEvaluationContext(WebApplicationService service, @Nullable RegisteredService registeredService,
                                          Principal principal, Credential credential, Authentication authentication) {
    }

    /**
     * Details of the client a login attempt comes from, as supplied to a tool. Blank values are treated as absent.
     *
     * @param clientIpAddress the client IP address
     * @param userAgent       the user agent
     * @param geoLocation     the location as latitude,longitude
     */
    public record CasMcpClient(@Nullable String clientIpAddress, @Nullable String userAgent, @Nullable String geoLocation) {
        /**
         * Build client details, treating blank values as absent.
         *
         * @param clientIpAddress the client IP address
         * @param userAgent       the user agent
         * @param geoLocation     the location as latitude,longitude
         * @return the client details
         */
        public static CasMcpClient of(final @Nullable String clientIpAddress, final @Nullable String userAgent,
                                      final @Nullable String geoLocation) {
            return new CasMcpClient(StringUtils.trimToNull(clientIpAddress), StringUtils.trimToNull(userAgent),
                StringUtils.trimToNull(geoLocation));
        }

        /**
         * Client details where nothing is known.
         *
         * @return the client details
         */
        public static CasMcpClient unknown() {
            return new CasMcpClient(null, null, null);
        }

        /**
         * Describe the client details for a tool result.
         *
         * @return the client details, with absent values as null
         */
        public Map<String, Object> describe() {
            val result = new LinkedHashMap<String, Object>();
            result.put("clientIpAddress", clientIpAddress);
            result.put("userAgent", userAgent);
            result.put("geoLocation", geoLocation);
            return result;
        }
    }

    /**
     * The stripped live request and its response.
     *
     * @param request  the request without parameters, headers, cookies or query string
     * @param response the response
     */
    public record CasMcpHttpExchange(HttpServletRequest request, HttpServletResponse response) {
    }

    private static final class ClientHttpServletRequest extends HttpServletRequestWrapper {
        private final CasMcpClient client;

        private final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        private final Map<String, String[]> parameters = new LinkedHashMap<>();

        ClientHttpServletRequest(final HttpServletRequest request, final CasMcpClient client) {
            super(request);
            this.client = client;
            if (client.userAgent() != null) {
                headers.put(HttpHeaders.USER_AGENT, client.userAgent());
            }
            if (client.geoLocation() != null) {
                parameters.put(GEOLOCATION_PARAMETER, new String[]{client.geoLocation()});
            }
        }

        @Override
        public @Nullable String getParameter(final String name) {
            val values = parameters.get(name);
            return values != null ? values[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Map.copyOf(parameters);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameters.keySet());
        }

        @Override
        public String @Nullable [] getParameterValues(final String name) {
            val values = parameters.get(name);
            return values != null ? values.clone() : null;
        }

        @Override
        public @Nullable String getHeader(final String name) {
            return headers.get(name);
        }

        @Override
        public Enumeration<String> getHeaders(final String name) {
            val value = headers.get(name);
            return value != null ? Collections.enumeration(List.of(value)) : Collections.emptyEnumeration();
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        @Override
        public int getIntHeader(final String name) {
            return -1;
        }

        @Override
        public long getDateHeader(final String name) {
            return -1;
        }

        @Override
        public Cookie @Nullable [] getCookies() {
            return null;
        }

        @Override
        public @Nullable String getQueryString() {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return Objects.requireNonNullElse(client.clientIpAddress(), UNKNOWN_CLIENT);
        }

        @Override
        public String getRemoteHost() {
            return getRemoteAddr();
        }
    }
}
