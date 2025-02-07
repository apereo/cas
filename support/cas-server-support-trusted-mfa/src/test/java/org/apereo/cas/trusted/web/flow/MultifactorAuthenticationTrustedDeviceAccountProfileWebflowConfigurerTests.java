package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.config.CasMultifactorAuthnTrustAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
@ImportAutoConfiguration(CasMultifactorAuthnTrustAutoConfiguration.class)
class MultifactorAuthenticationTrustedDeviceAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        val accountFlow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        assertNotNull(accountFlow);
        val state = (ViewState) accountFlow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertNotNull(state);

        val definition = state.getTransition("deleteTrustedDevice");
        assertNotNull(definition);

        val actionState = (ActionState) accountFlow.getState("deleteMultifactorTrustedDevice");
        assertNotNull(actionState);
        assertNotNull(actionState.getTransition(CasWebflowConstants.STATE_ID_SUCCESS));
        assertNotNull(actionState.getTransition(CasWebflowConstants.TRANSITION_ID_ERROR));
    }
}
