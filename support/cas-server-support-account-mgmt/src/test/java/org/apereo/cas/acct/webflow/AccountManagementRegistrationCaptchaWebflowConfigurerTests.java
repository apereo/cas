package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.config.CasAccountManagementWebflowAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Action;
import static org.apereo.cas.web.flow.CasWebflowConstants.ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_SUBMIT_ACCOUNT_REGISTRATION;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_VIEW_ACCOUNT_SIGNUP;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountManagementRegistrationCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ImportAutoConfiguration(CasAccountManagementWebflowAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.account-registration.core.crypto.enabled=false",
    "cas.account-registration.google-recaptcha.enabled=true"
})
@Tag("WebflowConfig")
class AccountManagementRegistrationCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA)
    private Action initCaptchaAction;

    @Test
    void verifyCaptcha() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        initCaptchaAction.execute(context);
        assertTrue(AccountRegistrationUtils.isAccountRegistrationCaptchaEnabled(context));
    }

    @Test
    void verifyOperation() {
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        val state = (TransitionableState) flow.getState(STATE_ID_SUBMIT_ACCOUNT_REGISTRATION);
        assertEquals(STATE_ID_VIEW_ACCOUNT_SIGNUP,
            state.getTransition(TRANSITION_ID_CAPTCHA_ERROR).getTargetStateId());
    }
}
