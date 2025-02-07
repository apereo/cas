package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasDelegatedAuthenticationOidcAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationOidcWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ImportAutoConfiguration(CasDelegatedAuthenticationOidcAutoConfiguration.class)
@Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowConfig")
@ExtendWith(CasTestExtension.class)
class DelegatedAuthenticationOidcWebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        var flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT));
        flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISH_LOGOUT));
    }

}

