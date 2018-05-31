package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@TestPropertySource(locations = {"classpath:/core.properties"})
@Import(CasSupportActionsConfiguration.class)
@Slf4j
public class InitialFlowSetupActionTests extends AbstractCentralAuthenticationServiceTests {
    @Autowired
    @Qualifier("initialFlowSetupAction")
    private Action action;

    @Test
    public void verifyNoServiceFound() throws Exception {
        final var context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        final var event = this.action.execute(context);
        assertNull(WebUtils.getService(context));
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyServiceFound() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        request.setParameter("service", "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final var event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertNotNull(WebUtils.getRegisteredService(context));
        assertEquals("success", event.getId());
    }
}
