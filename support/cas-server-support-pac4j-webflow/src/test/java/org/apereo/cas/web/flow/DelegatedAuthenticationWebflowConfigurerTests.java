package org.apereo.cas.web.flow;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.test.MockRequestControlContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Import({
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("WebflowConfig")
public class DelegatedAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION));

        val stopState = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
        val context = new MockRequestControlContext(flow);
        val request = new MockHttpServletRequest();
        request.addParameter("error_description", "fail");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        context.getFlowScope().put("rootCauseException", new RuntimeException());
        stopState.enter(context);
        assertTrue(context.getFlowScope().contains("code"));
        assertTrue(context.getFlowScope().contains("description"));
    }
}
