package org.apereo.cas.web.flow;

import org.apereo.cas.support.spnego.util.SpnegoConstants;

import lombok.val;
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
        assertTrue(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
    }
}
