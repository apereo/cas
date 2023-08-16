package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractGraphicalAuthenticationTests;
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
 * This is {@link PrepareForGraphicalAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
class PrepareForGraphicalAuthenticationActionTests extends AbstractGraphicalAuthenticationTests {
    @Test
    void verifyAction() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val event = prepareLoginAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_GUA_GET_USERID, event.getId());
    }

    @Test
    void verifyMissingAction() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        WebUtils.putGraphicalUserAuthenticationUsername(context, "casuser");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val event = prepareLoginAction.execute(context);
        assertNull(event);
    }
}
