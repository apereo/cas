package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(locations = {"classpath:/core.properties"})
public class InitialFlowSetupActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("initialFlowSetupAction")
    private Action action;

    @Test
    public void verifyNoServiceFound() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        val event = this.action.execute(context);
        assertNull(WebUtils.getService(context));
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyServiceFound() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setParameter("service", "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertNotNull(WebUtils.getRegisteredService(context));
        assertEquals("success", event.getId());
    }
}
