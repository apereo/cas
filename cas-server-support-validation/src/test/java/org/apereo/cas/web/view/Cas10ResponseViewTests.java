package org.apereo.cas.web.view;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.validation.ImmutableAssertion;
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

    private Cas10ResponseView view = new Cas10ResponseView();

    private Map<String, Object> model;

    @Before
    public void setUp() throws Exception {
        this.model = new HashMap<>();
        final List<Authentication> list = new ArrayList<>();
        list.add(TestUtils.getAuthentication("someothername"));
        this.model.put("assertion", new ImmutableAssertion(
                TestUtils.getAuthentication(), list,
                TestUtils.getService("TestService"), true));
    }

    @Test
    public void verifySuccessView() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.view.setSuccessResponse(true);
        this.view.render(this.model, new MockHttpServletRequest(), response
                );
        assertEquals("yes\ntest\n", response.getContentAsString());
    }

    @Test
    public void verifyFailureView() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        this.view.setSuccessResponse(false);
        this.view.render(this.model, new MockHttpServletRequest(),
                response);
        assertEquals("no\n\n", response.getContentAsString());
    }
}
