package org.apereo.cas.web.flow;

import org.apereo.cas.web.CasWebSecurityConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML2Web")
class SamlIdPWebflowConfigurerTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<Void> samlIdPProtocolEndpointConfigurer;

    @Test
    void verifyEndpoints() throws Throwable {
        assertFalse(samlIdPProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }
    
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}


