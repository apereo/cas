package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.config.PasswordManagementForgotUsernameConfiguration;
import org.apereo.cas.pm.config.PasswordManagementWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.mvc.servlet.FlowHandler;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordManagementWebflowConfigurerDisabledTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    PasswordManagementConfiguration.class,
    PasswordManagementWebflowConfiguration.class,
    PasswordManagementForgotUsernameConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.reset.security-questions-enabled=false",
    "cas.authn.pm.core.enabled=false"
})
@Tag("WebflowConfig")
public class PasswordManagementWebflowConfigurerDisabledTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("passwordResetHandlerAdapter")
    private HandlerAdapter passwordResetHandlerAdapter;

    @Autowired
    @Qualifier("verifySecurityQuestionsAction")
    private Action verifySecurityQuestionsAction;

    @Test
    public void verifyOperation() throws Exception {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_AUTHENTICATION_BLOCKED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_WORKSTATION);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INVALID_AUTHENTICATION_HOURS);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_PASSWORD_UPDATE_SUCCESS);
        assertNotNull(state);

        val handler = mock(FlowHandler.class);
        when(handler.getFlowId()).thenReturn(PasswordManagementWebflowConfigurer.FLOW_ID_PASSWORD_RESET);
        assertTrue(passwordResetHandlerAdapter.supports(handler));

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifySecurityQuestionsAction.execute(context).getId());

        verifyPasswordManagementStates(flow);
    }

    protected void verifyPasswordManagementStates(final Flow flow) {
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
        assertNotNull(state);
    }
}

