package org.apereo.cas.web.saml2;

import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationSaml2WebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Import(BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowConfig")
class DelegatedAuthenticationSaml2WebflowConfigurerTests extends BaseWebflowConfigurerTests {

    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        var flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_IDP_LOGOUT));
        flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGOUT);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISH_LOGOUT));
    }

}

