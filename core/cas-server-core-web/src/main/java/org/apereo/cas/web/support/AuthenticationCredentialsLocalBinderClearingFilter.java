package org.apereo.cas.web.support;

import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Servlet Filter for clearing thread local state of current credentials and authentication at the end of request/response
 * processing cycle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class AuthenticationCredentialsLocalBinderClearingFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            AuthenticationCredentialsLocalBinder.clear();
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        //noop
    }

    @Override
    public void destroy() {
        //noop
    }
}
