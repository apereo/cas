package org.apereo.cas.mcp;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
 * or a webflow request context built over it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class CasMcpEvaluationContextFactory {
    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final PrincipalResolver principalResolver;

    /**
     * Build the evaluation context for a person signing in to an application.
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
        val principal = Objects.requireNonNull(FunctionUtils.doUnchecked(() -> principalResolver.resolve(
                credential, Optional.empty(), Optional.empty(), Optional.of(service))),
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
        val attributes = RequestContextHolder.getRequestAttributes() instanceof final ServletRequestAttributes servletAttributes
            ? servletAttributes : null;
        val response = attributes != null ? attributes.getResponse() : null;
        if (attributes == null || response == null) {
            throw new IllegalStateException("This evaluation can only run within an HTTP request");
        }
        return new CasMcpHttpExchange(new EmptyHttpServletRequest(attributes.getRequest()), response);
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
     * The stripped live request and its response.
     *
     * @param request  the request without parameters, headers, cookies or query string
     * @param response the response
     */
    public record CasMcpHttpExchange(HttpServletRequest request, HttpServletResponse response) {
    }

    private static final class EmptyHttpServletRequest extends HttpServletRequestWrapper {
        EmptyHttpServletRequest(final HttpServletRequest request) {
            super(request);
        }

        @Override
        public @Nullable String getParameter(final String name) {
            return null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Map.of();
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.emptyEnumeration();
        }

        @Override
        public String @Nullable [] getParameterValues(final String name) {
            return null;
        }

        @Override
        public @Nullable String getHeader(final String name) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(final String name) {
            return Collections.emptyEnumeration();
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.emptyEnumeration();
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
    }
}
