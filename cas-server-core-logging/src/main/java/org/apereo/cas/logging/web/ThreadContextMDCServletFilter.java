package org.apereo.cas.logging.web;

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
import java.util.Map;
import java.util.TimeZone;

/**
 * This is {@link ThreadContextMDCServletFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ThreadContextMDCServletFilter implements Filter {
    private FilterConfig filterConfig;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
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
                MDC.put("requestParameter" + k, Arrays.toString(values));
            });

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
