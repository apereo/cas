package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.SpnegoConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.SpnegoWebflowActionsConfiguration;
import org.apereo.cas.web.flow.config.SpnegoWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpengoWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    SpnegoConfiguration.class,
    SpnegoWebflowConfiguration.class,
    SpnegoWebflowActionsConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class SpengoWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EVALUATE_SPNEGO_CLIENT);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SPNEGO);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_START_SPNEGO_AUTHENTICATE);
        assertNotNull(state);
    }
}
