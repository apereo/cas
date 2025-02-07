package org.apereo.cas.pm.web.flow;

import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementWebflowAutoConfiguration;
import org.apereo.cas.config.CasSupportActionsAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
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
import static org.apereo.cas.web.flow.CasWebflowConstants.ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_FORGOT_USERNAME_ACCT_INFO;
import static org.apereo.cas.web.flow.CasWebflowConstants.STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS;
import static org.apereo.cas.web.flow.CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ForgotUsernameCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ImportAutoConfiguration({
    CasSupportActionsAutoConfiguration.class,
    CasPasswordManagementAutoConfiguration.class,
    CasPasswordManagementWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.forgot-username.google-recaptcha.enabled=true",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.reset.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
    "cas.authn.pm.reset.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA"
})
@Tag("WebflowConfig")
class ForgotUsernameCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA)
    private Action initCaptchaAction;

    @Test
    void verifyCaptcha() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        initCaptchaAction.execute(context);
        assertTrue(WebUtils.isRecaptchaForgotUsernameEnabled(context));
    }

    @Test
    void verifyOperation() {
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS);
        assertEquals(STATE_ID_FORGOT_USERNAME_ACCT_INFO,
            state.getTransition(TRANSITION_ID_CAPTCHA_ERROR).getTargetStateId());
    }
}
