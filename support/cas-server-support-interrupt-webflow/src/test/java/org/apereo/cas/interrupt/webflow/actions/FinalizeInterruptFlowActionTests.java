package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FinalizeInterruptFlowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class FinalizeInterruptFlowActionTests {

    @Test
    public void verifyFinalizedInterruptBlocked() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val interrupt = InterruptResponse.interrupt();
        interrupt.setBlock(true);

        InterruptUtils.putInterruptIn(context, interrupt);
        WebUtils.putRegisteredService(context, CoreAuthenticationTestUtils.getRegisteredService());

        val action = new FinalizeInterruptFlowAction();
        assertThrows(UnauthorizedServiceException.class, () -> action.doExecute(context));
    }

    @Test
    public void verifyFinalizedInterruptBlockedUnauthzUrl() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new MockExternalContext());

        val interrupt = InterruptResponse.interrupt();
        interrupt.setBlock(true);

        InterruptUtils.putInterruptIn(context, interrupt);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        val strategy = new DefaultRegisteredServiceAccessStrategy(true, true);
        strategy.setUnauthorizedRedirectUrl(new URI("https://www.github.com"));
        registeredService.setAccessStrategy(strategy);
        WebUtils.putRegisteredService(context, registeredService);

        val action = new FinalizeInterruptFlowAction();
        val event = action.doExecute(context);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_STOP);
        assertTrue(context.getMockExternalContext().isResponseComplete());
        assertNotNull(context.getMockExternalContext().getExternalRedirectUrl());
    }

    @Test
    public void verifyFinalizedInterruptNonBlocked() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val interrupt = InterruptResponse.interrupt();

        InterruptUtils.putInterruptIn(context, interrupt);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putRegisteredService(context, CoreAuthenticationTestUtils.getRegisteredService());

        val action = new FinalizeInterruptFlowAction();
        val event = action.doExecute(context);
        assertEquals(event.getId(), CasWebflowConstants.TRANSITION_ID_SUCCESS);
        val authn = WebUtils.getAuthentication(context);
        assertTrue(authn.getAttributes().containsKey(InquireInterruptAction.AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT));
    }
}
