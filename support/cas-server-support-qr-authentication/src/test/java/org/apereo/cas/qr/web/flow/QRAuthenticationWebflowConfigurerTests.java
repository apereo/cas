package org.apereo.cas.qr.web.flow;

import org.apereo.cas.config.QRAuthenticationConfiguration;
import org.apereo.cas.config.TokenCoreConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Import({
    QRAuthenticationConfiguration.class,
    TokenCoreConfiguration.class
})
@Tag("WebflowConfig")
public class QRAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(QRAuthenticationWebflowConfigurer.STATE_ID_VALIDATE_QR_TOKEN));
    }
}
