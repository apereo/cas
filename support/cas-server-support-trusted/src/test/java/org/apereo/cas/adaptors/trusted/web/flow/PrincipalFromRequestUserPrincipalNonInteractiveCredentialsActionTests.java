package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.5
 */
@Tag("WebflowActions")
public class PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {

    @Autowired
    @Qualifier("principalFromRemoteUserPrincipalAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    public void verifyRemoteUserExists() throws Exception {
        val request = new MockHttpServletRequest();
        request.setUserPrincipal(() -> "test");

        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }

    @Test
    public void verifyRemoteUserDoesntExists() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, this.action.execute(context).getId());
    }

}
