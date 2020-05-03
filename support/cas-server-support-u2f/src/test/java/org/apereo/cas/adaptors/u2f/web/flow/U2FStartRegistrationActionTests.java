package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FStartRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseU2FWebflowActionTests.SharedTestConfiguration.class)
@Tag("Webflow")
public class U2FStartRegistrationActionTests extends BaseU2FWebflowActionTests {
    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val response = new MockHttpServletResponse();
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(id), context);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, u2fStartRegistrationAction.execute(context).getId());
        assertNotNull(context.getFlowScope().get("u2fReg"));
    }
}
