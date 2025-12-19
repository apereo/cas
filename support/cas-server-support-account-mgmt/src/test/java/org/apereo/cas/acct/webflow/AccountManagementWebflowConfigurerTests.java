package org.apereo.cas.acct.webflow;

import module java.base;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasThymeleafAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountManagementWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ImportAutoConfiguration({
    CasThymeleafAutoConfiguration.class,
    CasAccountManagementWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
class AccountManagementWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_SENT_ACCOUNT_SIGNUP_INFO));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW));

        val subflow = (SubflowState) flow.getState(CasWebflowConstants.STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW);
        assertNotNull(subflow);

        val regFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(AccountManagementWebflowConfigurer.FLOW_ID_ACCOUNT_REGISTRATION);
        val context = MockRequestContext.create(applicationContext);
        context.setActiveFlow(regFlow);

        val completeState = (ViewState) regFlow.getState(CasWebflowConstants.STATE_ID_COMPLETE_ACCOUNT_REGISTRATION);
        completeState.enter(context);

        assertNotNull(WebUtils.getPasswordPolicyPattern(context));
        assertEquals(2, AccountRegistrationUtils.getAccountRegistrationSecurityQuestionsCount(context));
    }
}
