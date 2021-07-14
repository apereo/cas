package org.apereo.cas.web.flow;

import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;

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
@Tag("SAML")
public class SamlIdPWebflowConfigurerTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPProtocolEndpointConfigurer")
    private ProtocolEndpointWebSecurityConfigurer<Void> samlIdPProtocolEndpointConfigurer;

    @Test
    public void verifyEndpoints() {
        assertFalse(samlIdPProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }
    
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
    }
}


