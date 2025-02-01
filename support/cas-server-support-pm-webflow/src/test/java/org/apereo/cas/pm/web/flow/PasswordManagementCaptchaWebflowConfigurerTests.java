package org.apereo.cas.pm.web.flow;

import org.apereo.cas.config.CasPasswordManagementAutoConfiguration;
import org.apereo.cas.config.CasPasswordManagementWebflowAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ImportAutoConfiguration({
    CasPasswordManagementAutoConfiguration.class,
    CasPasswordManagementWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.google-recaptcha.enabled=true",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.reset.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
    "cas.authn.pm.reset.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA"
})
@Tag("WebflowConfig")
class PasswordManagementCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA)
    private Action initCaptchaAction;

    @Test
    void verifyCaptcha() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        initCaptchaAction.execute(context);
        assertTrue(WebUtils.isRecaptchaPasswordManagementEnabled(context));
    }

    @Test
    void verifyOperation() {
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);
        assertEquals(CasWebflowConstants.STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO,
            state.getTransition(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR).getTargetStateId());
    }
}
