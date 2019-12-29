package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.config.CasConsentWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConsentWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasConsentCoreConfiguration.class,
    CasConsentWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class ConsentWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(ConsentWebflowConfigurer.STATE_ID_CONSENT_CONFIRM));
        assertTrue(flow.containsState(ConsentWebflowConfigurer.VIEW_ID_CONSENT_VIEW));
    }
}
