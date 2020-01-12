package org.apereo.cas.web.flow;

import org.apereo.cas.config.MultiphaseAuthenticationConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultiphaseAuthenticationWebflowConfigurerTests}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Import({
    ThymeleafAutoConfiguration.class,
    MultiphaseAuthenticationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class MultiphaseAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(
                MultiphaseAuthenticationWebflowConfigurer.STATE_ID_MULTIPHASE_GET_USERID);
        assertNotNull(state);
    }
}
