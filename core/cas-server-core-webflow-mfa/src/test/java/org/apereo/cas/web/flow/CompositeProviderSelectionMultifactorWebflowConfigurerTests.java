package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeProviderSelectionMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ImportAutoConfiguration({
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true",
    "cas.authn.mfa.core.provider-selection.provider-selection-optional=true"
})
@Tag("WebflowMfaConfig")
class CompositeProviderSelectionMultifactorWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        assertNotNull(state.getTransition(CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE));

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MFA_PROVIDER_SELECTED);
        assertNotNull(state.getTransition(CasWebflowConstants.TRANSITION_ID_SKIP));

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        assertNotNull(state.getTransition(CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE));
    }
}
