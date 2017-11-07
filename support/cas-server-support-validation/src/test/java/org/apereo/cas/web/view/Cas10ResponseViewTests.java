package org.apereo.cas.web.view;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Cas10ResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class Cas10ResponseViewTests {

    private Map<String, Object> model;

    @Before
    public void setUp() {
        this.model = new HashMap<>();
        final List<Authentication> list = new ArrayList<>();
        list.add(CoreAuthenticationTestUtils.getAuthentication("someothername"));
        this.model.put("assertion", new DefaultAssertionBuilder(
                CoreAuthenticationTestUtils.getAuthentication()).with(list).with(
                CoreAuthenticationTestUtils.getService("TestService")).with(true).build());
    }

    @Test
    public void verifySuccessView() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Cas10ResponseView view = new Cas10ResponseView(true, null,
                null, null, null);
        view.render(this.model, new MockHttpServletRequest(), response);
        assertEquals("yes\ntest\n", response.getContentAsString());
    }

    @Test
    public void verifyFailureView() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final Cas10ResponseView view = new Cas10ResponseView(false, null,
                null, null, null);
        view.render(this.model, new MockHttpServletRequest(), response);
        assertEquals("no\n\n", response.getContentAsString());
    }
}
