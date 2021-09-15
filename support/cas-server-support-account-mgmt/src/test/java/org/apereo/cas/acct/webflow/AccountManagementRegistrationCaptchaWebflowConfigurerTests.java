package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowConfiguration;
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
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.apereo.cas.web.flow.CasWebflowConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountManagementRegistrationCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Import({
    CasAccountManagementWebflowConfiguration.class,
    CasAccountManagementWebflowConfiguration.CasAccountManagementRegistrationCaptchaConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.account-registration.core.crypto.enabled=false",
    "cas.account-registration.google-recaptcha.enabled=true"
})
@Tag("WebflowConfig")
public class AccountManagementRegistrationCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("accountMgmtRegistrationInitializeCaptchaAction")
    private Action initCaptchaAction;

    @Test
    public void verifyCaptcha() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        initCaptchaAction.execute(context);
        assertTrue(AccountRegistrationUtils.isAccountRegistrationCaptchaEnabled(context));
    }

    @Test
    public void verifyOperation() {
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION);
        assertEquals(CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP,
            state.getTransition(TRANSITION_ID_CAPTCHA_ERROR).getTargetStateId());
    }
}
