package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

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
 * This is {@link VerifySecurityQuestionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class VerifySecurityQuestionsActionTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter("q0", "securityAnswer1");
        context.getFlowScope().put("username", "casuser");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());
    }

    @Test
    public void verifyFailsAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.getFlowScope().put("username", "casuser");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifySecurityQuestionsAction.execute(context).getId());
    }
}
