package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.category.MailCategory;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link VerifyPasswordResetRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfContinuousIntegration
@EnabledIfPortOpen(port = 25000)
@Category(MailCategory.class)
public class VerifyPasswordResetRequestActionTests extends BasePasswordManagementActionTests {
    @Test
    public void verifyAction() {
        try {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals("error", verifyPasswordResetRequestAction.execute(context).getId());

            request.setRemoteAddr("1.2.3.4");
            request.setLocalAddr("1.2.3.4");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));

            val token = passwordManagementService.createToken("casuser");
            request.addParameter(SendPasswordResetInstructionsAction.PARAMETER_NAME_TOKEN, token);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals("success", verifyPasswordResetRequestAction.execute(context).getId());

            assertTrue(context.getFlowScope().contains("questionsEnabled"));
            assertTrue(context.getFlowScope().contains("username"));
            assertTrue(context.getFlowScope().contains("token"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
