package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.config.CasConsentWebflowAutoConfiguration;
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
    CasConsentCoreAutoConfiguration.class,
    CasConsentWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
class ConsentWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(ConsentWebflowConfigurer.STATE_ID_CONSENT_CONFIRM));
        assertTrue(flow.containsState(ConsentWebflowConfigurer.VIEW_ID_CONSENT_VIEW));
    }
}
