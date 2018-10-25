package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class WsFederationActionTests extends BaseWsFederationWebflowTests {
    @Test
    public void verifyRequestOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        wsFederationAction.execute(context);
        assertTrue(context.getFlowScope().contains(WsFederationRequestBuilder.PARAMETER_NAME_WSFED_CLIENTS));
    }
}
