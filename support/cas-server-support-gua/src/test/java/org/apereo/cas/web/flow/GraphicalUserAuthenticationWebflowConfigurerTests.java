package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.config.CasGraphicalUserAuthenticationAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
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
@ImportAutoConfiguration(CasGraphicalUserAuthenticationAutoConfiguration.class)
@TestPropertySource(properties = "cas.authn.gua.simple.casuser=classpath:image.jpg")
@Tag("WebflowConfig")
class GraphicalUserAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);

        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_ACCEPT_GUA);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_GUA_DISPLAY_USER_GFX);
        assertNotNull(state);

        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_GUA_GET_USERID);
        assertNotNull(state);
    }
}
