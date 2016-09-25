package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CasSupportActionsConfiguration.class,
                CasCoreWebflowConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCookieConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreServicesConfiguration.class})
@TestPropertySource(locations = {"classpath:/core.properties"})
@ContextConfiguration(locations = "classpath:/core-context.xml")
public class InitialFlowSetupActionTests {
    @Autowired
    @Qualifier("initialFlowSetupAction")
    private Action action;

    @Test
    public void verifyNoServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));
        final Event event = this.action.execute(context);
        assertNull(WebUtils.getService(context));
        assertEquals("success", event.getId());
    }

    @Test
    public void verifyServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final Event event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertNotNull(WebUtils.getRegisteredService(context));
        assertEquals("success", event.getId());
    }
}
