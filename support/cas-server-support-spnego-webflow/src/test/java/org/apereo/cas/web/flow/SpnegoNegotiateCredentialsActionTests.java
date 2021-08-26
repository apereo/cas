package org.apereo.cas.web.flow;

import org.apereo.cas.support.spnego.util.SpnegoConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpnegoNegotiateCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Spnego")
public class SpnegoNegotiateCredentialsActionTests extends AbstractSpnegoTests {
    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "MSIE");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        negociateSpnegoAction.execute(context);
        assertNotNull(response.getHeader(SpnegoConstants.HEADER_AUTHENTICATE));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    public void verifyEmptyAgent() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, negociateSpnegoAction.execute(context).getId());

        request.addHeader("User-Agent", "UnknownBrowser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, negociateSpnegoAction.execute(context).getId());
    }

    @Test
    public void verifyBadAuthzHeader() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("User-Agent", "MSIE");
        request.addHeader(SpnegoConstants.HEADER_AUTHORIZATION, SpnegoConstants.NEGOTIATE + " XYZ");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, negociateSpnegoAction.execute(context).getId());
    }
}
