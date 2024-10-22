package org.apereo.cas.logging.web;

import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

/**
 * This is {@link ThreadContextMDCServletFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ThreadContextMDCServletFilter implements Filter {

    private static final String[] EXCLUDED_HEADER_NAMES = new String[]{"cookie", "authorization"};

    private final ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    private final ObjectProvider<CasCookieBuilder> ticketGrantingTicketCookieGenerator;

    private static void addContextAttribute(final String attributeName, final Object value) {
        val result = Optional.ofNullable(value).map(Object::toString).orElse(null);
        if (StringUtils.isNotBlank(result)) {
            MDC.put(attributeName, result);
        }
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            val request = (HttpServletRequest) servletRequest;
            val response = (HttpServletResponse) servletResponse;

            addContextAttribute("remoteAddress", request.getRemoteAddr());
            addContextAttribute("remoteUser", request.getRemoteUser());
            addContextAttribute("serverName", request.getServerName());
            addContextAttribute("serverPort", String.valueOf(request.getServerPort()));
            addContextAttribute("locale", request.getLocale().getDisplayName());
            addContextAttribute("contentType", request.getContentType());
            addContextAttribute("contextPath", request.getContextPath());
            addContextAttribute("localAddress", request.getLocalAddr());
            addContextAttribute("localPort", String.valueOf(request.getLocalPort()));
            addContextAttribute("remotePort", String.valueOf(request.getRemotePort()));
            addContextAttribute("pathInfo", request.getPathInfo());
            addContextAttribute("protocol", request.getProtocol());
            addContextAttribute("authType", request.getAuthType());
            addContextAttribute("method", request.getMethod());
            addContextAttribute("queryString", request.getQueryString());
            addContextAttribute("requestUri", request.getRequestURI());
            addContextAttribute("scheme", request.getScheme());
            addContextAttribute("timezone", TimeZone.getDefault().getDisplayName());

            val requestId = UUID.randomUUID().toString();
            addContextAttribute("requestId", requestId);
            request.setAttribute("requestId", requestId);
            response.setHeader("X-RequestId", requestId);

            Optional.ofNullable(request.getSession(false)).ifPresent(session -> {
                addContextAttribute("sessionId", session.getId());
                request.setAttribute("sessionId", session.getId());
                response.setHeader("X-SessionId", session.getId());
            });
            
            val params = request.getParameterMap();
            params.keySet()
                .stream()
                .filter(parameterName -> !StringUtils.containsIgnoreCase(parameterName, "password"))
                .forEach(parameterName -> {
                    val values = params.get(parameterName);
                    addContextAttribute(parameterName, Arrays.toString(values));
                });

            Collections.list(request.getAttributeNames()).forEach(a -> addContextAttribute(a, request.getAttribute(a)));
            val requestHeaderNames = request.getHeaderNames();
            FunctionUtils.doIfNotNull(requestHeaderNames,
                __ -> Collections.list(requestHeaderNames)
                    .stream()
                    .filter(h -> !StringUtils.containsAnyIgnoreCase(h, EXCLUDED_HEADER_NAMES))
                    .forEach(h -> addContextAttribute(h, request.getHeader(h))));
            val cookieValue = ticketGrantingTicketCookieGenerator.getObject().retrieveCookieValue(request);
            if (StringUtils.isNotBlank(cookieValue)) {
                val p = ticketRegistrySupport.getObject().getAuthenticatedPrincipalFrom(cookieValue);
                FunctionUtils.doIfNotNull(p, __ -> addContextAttribute("principal", p.getId()));
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }
}
