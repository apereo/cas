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
 * This is {@link CheckConsentRequiredActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CheckConsentRequiredActionTests extends BaseConsentActionTests {

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("consentService"));
        assertEquals(CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, checkConsentRequiredAction.execute(context).getId());
        assertTrue(context.getFlowScope().contains("attributes"));
        assertTrue(context.getFlowScope().contains("principal"));
        assertTrue(context.getFlowScope().contains("service"));
        assertTrue(context.getFlowScope().contains("option"));
        assertTrue(context.getFlowScope().contains("reminder"));
        assertTrue(context.getFlowScope().contains("reminderTimeUnit"));
    }
}
