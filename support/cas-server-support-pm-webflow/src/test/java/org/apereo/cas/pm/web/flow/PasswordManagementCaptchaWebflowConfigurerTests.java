package org.apereo.cas.pm.web.flow;

import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.apereo.cas.pm.config.PasswordManagementWebflowConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    PasswordManagementConfiguration.class,
    PasswordManagementWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.pm.captcha-enabled=true",
    "cas.authn.pm.enabled=true",
    "cas.authn.pm.reset.crypto.encryption.key=qLhvLuaobvfzMmbo9U_bYA",
    "cas.authn.pm.reset.crypto.signing.key=oZeAR5pEXsolruu4OQYsQKxf-FCvFzSsKlsVaKmfIl6pNzoPm6zPW94NRS1af7vT-0bb3DpPBeksvBXjloEsiA"
})
@Tag("WebflowConfig")
public class PasswordManagementCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS);
        assertEquals(CasWebflowConstants.VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO,
            state.getTransition(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR).getTargetStateId());
    }
}
