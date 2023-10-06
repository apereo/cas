package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegationWebflowUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Utility")
class DelegationWebflowUtilsTests {

    @Test
    void verifyOperation() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val flow = new Flow("mockFlow");
        val flowSession = new MockFlowSession(flow);
        flowSession.setParent(new MockFlowSession(flow));
        val mockExecutionContext = new MockFlowExecutionContext(flowSession);
        context.setFlowExecutionContext(mockExecutionContext);

        assertTrue(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context).isEmpty());
        DelegationWebflowUtils.putDelegatedClientAuthenticationResolvedCredentials(context, List.of("C1"));
        assertNotNull(DelegationWebflowUtils.getDelegatedClientAuthenticationResolvedCredentials(context, String.class));
    }

}
