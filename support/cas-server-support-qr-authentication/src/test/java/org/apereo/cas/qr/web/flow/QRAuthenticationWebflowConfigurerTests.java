package org.apereo.cas.qr.web.flow;

import org.apereo.cas.config.CasQRAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasTokenCoreAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@ImportAutoConfiguration({
    CasQRAuthenticationAutoConfiguration.class,
    CasTokenCoreAutoConfiguration.class
})
@Tag("WebflowConfig")
class QRAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(QRAuthenticationWebflowConfigurer.STATE_ID_VALIDATE_QR_TOKEN));
    }
}
