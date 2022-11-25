package org.apereo.cas.web.support;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

/**
 * Servlet Filter for clearing thread local state of current credentials and authentication at the end of request/response
 * processing cycle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class AuthenticationCredentialsThreadLocalBinderClearingFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AuthenticationCredentialsThreadLocalBinder.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
