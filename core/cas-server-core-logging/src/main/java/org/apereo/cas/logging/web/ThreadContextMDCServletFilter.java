package org.apereo.cas.logging.web;

import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

/**
 * This is {@link ThreadContextMDCServletFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ThreadContextMDCServletFilter implements Filter {

    private final TicketRegistrySupport ticketRegistrySupport;
    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private static void addContextAttribute(final String attributeName, final Object value) {
        val result = Optional.ofNullable(value).map(Object::toString).orElse(null);
        if (StringUtils.isNotBlank(result)) {
            MDC.put(attributeName, result);
        }
    }

    /**
     * Does nothing.
     *
     * @param filterConfig filter initial configuration. Ignored.
     */
    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            val request = (HttpServletRequest) servletRequest;

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

            val params = request.getParameterMap();
            params.keySet()
                .stream()
                .filter(k -> !k.equalsIgnoreCase("password"))
                .forEach(k -> {
                    val values = params.get(k);
                    addContextAttribute(k, Arrays.toString(values));
                });

            Collections.list(request.getAttributeNames()).forEach(a -> addContextAttribute(a, request.getAttribute(a)));
            val requestHeaderNames = request.getHeaderNames();
            if (requestHeaderNames != null) {
                Collections.list(requestHeaderNames).forEach(h -> addContextAttribute(h, request.getHeader(h)));
            }

            val cookieValue = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
            if (StringUtils.isNotBlank(cookieValue)) {
                val p = this.ticketRegistrySupport.getAuthenticatedPrincipalFrom(cookieValue);
                if (p != null) {
                    addContextAttribute("principal", p.getId());
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
    }
}
