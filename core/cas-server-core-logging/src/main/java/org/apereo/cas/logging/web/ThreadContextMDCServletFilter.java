package org.apereo.cas.logging.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
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
import java.util.Map;
import java.util.TimeZone;
import java.util.Enumeration;

/**
 * This is {@link ThreadContextMDCServletFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ThreadContextMDCServletFilter implements Filter {

    private final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private final TicketRegistrySupport ticketRegistrySupport;

    public ThreadContextMDCServletFilter(final TicketRegistrySupport ticketRegistrySupport,
                                         final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.ticketRegistrySupport = ticketRegistrySupport;
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
            final HttpServletRequest request = (HttpServletRequest) servletRequest;

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

            final Map<String, String[]> params = request.getParameterMap();
            params.keySet().forEach(k -> {
                final String[] values = params.get(k);
                addContextAttribute(k, Arrays.toString(values));
            });
            
            Collections.list(request.getAttributeNames()).forEach(a -> addContextAttribute(a, request.getAttribute(a)));
            final Enumeration<String> requestHeaderNames = request.getHeaderNames();
            if (requestHeaderNames != null) {
                Collections.list(requestHeaderNames).forEach(h -> addContextAttribute(h, request.getHeader(h)));
            }

            final String cookieValue = this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
            if (StringUtils.isNotBlank(cookieValue)) {
                final Principal p = this.ticketRegistrySupport.getAuthenticatedPrincipalFrom(cookieValue);
                if (p != null) {
                    addContextAttribute("principal", p.getId());
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }

    private static void addContextAttribute(final String attributeName, final Object value) {
        if (value != null && StringUtils.isNotBlank(value.toString())) {
            MDC.put(attributeName, value.toString());
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
    }
}
