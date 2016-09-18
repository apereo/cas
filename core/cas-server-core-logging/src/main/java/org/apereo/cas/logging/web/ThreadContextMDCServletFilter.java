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
     * @param filterConfig filter initial configuration. Ignored.
     * @throws ServletException never thrown in this case.
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            final HttpServletRequest request = (HttpServletRequest) servletRequest;

            MDC.put("remoteAddress", request.getRemoteAddr());
            MDC.put("remoteUser", request.getRemoteUser());
            MDC.put("serverName", request.getServerName());
            MDC.put("serverPort", String.valueOf(request.getServerPort()));
            MDC.put("locale", request.getLocale().getDisplayName());
            MDC.put("contentType", request.getContentType());
            MDC.put("contextPath", request.getContextPath());
            MDC.put("localAddress", request.getLocalAddr());
            MDC.put("localPort", String.valueOf(request.getLocalPort()));
            MDC.put("remotePort", String.valueOf(request.getRemotePort()));
            MDC.put("pathInfo", request.getPathInfo());
            MDC.put("protocol", request.getProtocol());
            MDC.put("authType", request.getAuthType());
            MDC.put("method", request.getMethod());
            MDC.put("queryString", request.getQueryString());
            MDC.put("requestUri", request.getRequestURI());
            MDC.put("scheme", request.getScheme());
            MDC.put("timezone", TimeZone.getDefault().getDisplayName());

            final Map<String, String[]> params = request.getParameterMap();
            params.keySet().forEach(k -> {
                final String[] values = params.get(k);
                MDC.put(k, Arrays.toString(values));
            });
            
            Collections.list(request.getAttributeNames())
                    .forEach(a -> MDC.put(a, request.getAttribute(a).toString()));
            
            final String cookieValue = 
                this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
            if (StringUtils.isNotEmpty(cookieValue)) {
                final Principal p = this.ticketRegistrySupport.getAuthenticatedPrincipalFrom(cookieValue);
                if (p != null) {
                    MDC.put("principal", p.getId());
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
