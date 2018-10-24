package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link PrepareForGraphicalAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PrepareForGraphicalAuthenticationActionTests extends AbstractGraphicalAuthenticationActionTests {
    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val event = initializeLoginAction.execute(context);
        assertEquals(GraphicalUserAuthenticationWebflowConfigurer.TRANSITION_ID_GUA_GET_USERID, event.getId());
    }
}
