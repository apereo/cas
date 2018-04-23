package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link InitPasswordResetActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class, port = 25000)
public class InitPasswordResetActionTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyAction() {
        try {
            final MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            final String token = passwordManagementService.createToken("casuser");
            final MockRequestContext context = new MockRequestContext();

            context.getFlowScope().put("token", token);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals("success", initPasswordResetAction.execute(context).getId());
            final UsernamePasswordCredential c = WebUtils.getCredential(context, UsernamePasswordCredential.class);
            assertNotNull(c);
            assertEquals("casuser", c.getUsername());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
