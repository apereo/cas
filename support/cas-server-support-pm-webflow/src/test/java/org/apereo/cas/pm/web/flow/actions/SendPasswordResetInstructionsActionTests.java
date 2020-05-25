package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;
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
 * This is {@link SendPasswordResetInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
public class SendPasswordResetInstructionsActionTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyAction() {
        try {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
