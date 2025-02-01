package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasConsentCoreAutoConfiguration;
import org.apereo.cas.config.CasConsentWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConsentAccountProfileWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ImportAutoConfiguration({
    CasConsentCoreAutoConfiguration.class,
    CasConsentWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
class ConsentAccountProfileWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        assertNotNull(flow);
        val view = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW);
        assertTrue(view.getRenderActionList().get(1).toString().contains(CasWebflowConstants.ACTION_ID_CONSENT_ACCOUNT_PROFILE_PREPARE));
    }
}
