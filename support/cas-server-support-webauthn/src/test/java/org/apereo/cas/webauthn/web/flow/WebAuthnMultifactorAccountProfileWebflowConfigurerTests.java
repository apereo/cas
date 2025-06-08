package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnMultifactorAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaConfig")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
class WebAuthnMultifactorAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val accountView = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertEquals(4, accountView.getRenderActionList().size());
        assertTrue(accountView.getRenderActionList().get(0).toString().contains(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE));
        val registrationState = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_WEBAUTHN_VIEW_REGISTRATION);
        assertNotNull(registrationState);
        assertEquals(5, registrationState.getEntryActionList().size());
        assertNotNull(flow.getState(CasWebflowConstants.STATE_ID_SAVE_REGISTRATION));
    }
}
