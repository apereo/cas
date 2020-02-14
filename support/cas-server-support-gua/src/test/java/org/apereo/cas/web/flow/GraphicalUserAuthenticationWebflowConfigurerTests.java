package org.apereo.cas.web.flow;

import org.apereo.cas.gua.config.GraphicalUserAuthenticationConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    GraphicalUserAuthenticationConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@TestPropertySource(properties = "cas.authn.gua.resource.location=classpath:example.png")
@Tag("Webflow")
public class GraphicalUserAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(GraphicalUserAuthenticationWebflowConfigurer.STATE_ID_ACCEPT_GUA);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(GraphicalUserAuthenticationWebflowConfigurer.STATE_ID_GUA_DISPLAY_USER_GFX);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(GraphicalUserAuthenticationWebflowConfigurer.STATE_ID_GUA_GET_USERID);
        assertNotNull(state);
    }
}
