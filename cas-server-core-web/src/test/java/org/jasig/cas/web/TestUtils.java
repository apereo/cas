package org.jasig.cas.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * This is {@link TestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TestUtils {

    private TestUtils() {}

    public static MockRequestContext getContext() {
        return getContext(new MockHttpServletRequest());
    }

    public static MockRequestContext getContext(
            final MockHttpServletRequest request) {
        return getContext(request, new MockHttpServletResponse());
    }

    public static MockRequestContext getContext(
            final MockHttpServletRequest request,
            final MockHttpServletResponse response) {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        return context;
    }

}
