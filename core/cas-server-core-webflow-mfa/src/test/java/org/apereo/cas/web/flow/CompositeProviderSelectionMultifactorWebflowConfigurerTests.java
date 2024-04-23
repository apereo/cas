package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
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
@Import({
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class
})
@TestPropertySource(properties = "cas.authn.mfa.core.provider-selection.provider-selection-enabled=true")
@Tag("WebflowMfaConfig")
class CompositeProviderSelectionMultifactorWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        assertNotNull(state.getTransition(CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE));

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        assertNotNull(state.getTransition(CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE));
    }
}
