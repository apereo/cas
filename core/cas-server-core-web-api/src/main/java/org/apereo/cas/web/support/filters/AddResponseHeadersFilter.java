package org.apereo.cas.web.support.filters;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Allows users to easily inject the default security headers to assist in protecting the application.
 * The default for is to include the following headers:
 * &lt;pre&gt;
 * Cache-Control: no-cache, no-store, max-age=0, must-revalidate
 * Pragma: no-cache
 * Expires: 0
 * X-Content-Type-Options: nosniff
 * Strict-Transport-Security: max-age=15768000 ; includeSubDomains
 * X-Frame-Options: DENY
 * X-XSS-Protection: 1; mode=block
 * &lt;/pre&gt;
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Setter
@Slf4j
@Getter
@SuppressWarnings("JdkObsolete")
public class AddResponseHeadersFilter extends AbstractSecurityFilter implements Filter {
    private Map<String, String> headersMap = new LinkedHashMap<>();

    @Override
    public void init(final FilterConfig filterConfig) {
        val initParamNames = filterConfig.getInitParameterNames();
        while (initParamNames.hasMoreElements()) {
            val paramName = initParamNames.nextElement();
            val paramValue = filterConfig.getInitParameter(paramName);
            this.headersMap.put(paramName, paramValue);
        }
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof final HttpServletResponse httpServletResponse) {
            for (val entry : this.headersMap.entrySet()) {
                LOGGER.debug("Adding parameter [{}] with value [{}]", entry.getKey(), entry.getValue());
                httpServletResponse.addHeader(entry.getKey(), entry.getValue());
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
