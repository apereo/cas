package org.jasig.cas.web.view;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.validation.ImmutableAssertion;
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

    private final Cas10ResponseView view = new Cas10ResponseView();

    private Map<String, Object> model;

    @Before
    public void setUp() throws Exception {
        this.model = new HashMap<>();
        final List<Authentication> list = new ArrayList<>();
        list.add(org.jasig.cas.authentication.TestUtils.getAuthentication("someothername"));
        this.model.put("assertion", new ImmutableAssertion(
                org.jasig.cas.authentication.TestUtils.getAuthentication(), list,
                org.jasig.cas.authentication.TestUtils.getService("TestService"), true));
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
