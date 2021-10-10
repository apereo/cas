package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestControlContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountManagementWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Import({
    CasAccountManagementWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("WebflowConfig")
public class AccountManagementWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW));

        val subflow = (SubflowState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW);
        assertNotNull(subflow);

        val regFlow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(AccountManagementWebflowConfigurer.FLOW_ID_ACCOUNT_REGISTRATION);
        val context = new MockRequestControlContext(regFlow);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val completeState = (ViewState) regFlow.getState(CasWebflowConstants.STATE_ID_COMPLETE_ACCOUNT_REGISTRATION);
        completeState.enter(context);

        assertNotNull(WebUtils.getPasswordPolicyPattern(context));
        assertEquals(2, AccountRegistrationUtils.getAccountRegistrationSecurityQuestionsCount(context));
    }
}
