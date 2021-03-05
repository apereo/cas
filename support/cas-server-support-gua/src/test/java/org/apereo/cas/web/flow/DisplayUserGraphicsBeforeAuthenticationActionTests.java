package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractGraphicalAuthenticationTests;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DisplayUserGraphicsBeforeAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class DisplayUserGraphicsBeforeAuthenticationActionTests extends AbstractGraphicalAuthenticationTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter("username", "casuser");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val event = displayUserGraphicsBeforeAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue(WebUtils.containsGraphicalUserAuthenticationImage(context));
        assertTrue(WebUtils.containsGraphicalUserAuthenticationUsername(context));
    }

    @Test
    public void verifyMissingUser() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertThrows(UnauthorizedServiceException.class, () -> displayUserGraphicsBeforeAuthenticationAction.execute(context));
    }

}
