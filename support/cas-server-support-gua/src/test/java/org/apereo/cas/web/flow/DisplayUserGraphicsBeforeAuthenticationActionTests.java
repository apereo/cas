package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link DisplayUserGraphicsBeforeAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DisplayUserGraphicsBeforeAuthenticationActionTests extends AbstractGraphicalAuthenticationActionTests {
    @Test
    public void verifyAction() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        request.addParameter("username", "casuser");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        final var event = displayUserGraphicsBeforeAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue(WebUtils.containsGraphicalUserAuthenticationImage(context));
        assertTrue(WebUtils.containsGraphicalUserAuthenticationUsername(context));
    }
}
