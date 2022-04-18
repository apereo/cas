package org.apereo.cas.web.flow;

import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC")
public class DelegatedAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION));
    }
}
