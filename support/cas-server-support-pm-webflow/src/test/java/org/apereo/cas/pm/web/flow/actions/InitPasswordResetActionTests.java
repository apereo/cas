package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InitPasswordResetActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Mail")
public class InitPasswordResetActionTests extends BasePasswordManagementActionTests {

    @Test
    public void verifyAction() throws Exception {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("1.2.3.4");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));

        val token = passwordManagementService.createToken("casuser");
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());

        context.getFlowScope().put("token", token);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initPasswordResetAction.execute(context).getId());
        val c = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        assertNotNull(c);
        assertEquals("casuser", c.getUsername());
    }

    @Test
    public void verifyActionUserlessToken() throws Exception {
        val request = new MockHttpServletRequest();
        val token = passwordManagementService.createToken(StringUtils.EMPTY);
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put("token", token);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());
    }
}
