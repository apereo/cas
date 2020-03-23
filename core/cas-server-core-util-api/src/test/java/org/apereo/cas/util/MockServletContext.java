package org.apereo.cas.util;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

import java.util.EventListener;

import static org.mockito.Mockito.*;

/**
 * This is {@link MockServletContext}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class MockServletContext extends org.springframework.mock.web.MockServletContext {
    @Override
    public ServletRegistration.Dynamic addServlet(final String servletName, final Servlet servlet) {
        return mock(ServletRegistration.Dynamic.class);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(final String servletName, final Class<? extends Servlet> servletClass) {
        return mock(ServletRegistration.Dynamic.class);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(final String filterName, final String className) {
        return mock(FilterRegistration.Dynamic.class);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(final String filterName, final Filter filter) {
        return mock(FilterRegistration.Dynamic.class);
    }

    @Override
    public <T extends EventListener> void addListener(final T t) {
    }
}
