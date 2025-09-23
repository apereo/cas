package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasSpnegoAutoConfiguration;
import org.apereo.cas.config.CasSpnegoWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SpengoWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ImportAutoConfiguration({
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasSpnegoAutoConfiguration.class,
    CasSpnegoWebflowAutoConfiguration.class
})
@Tag("Spnego")
@TestPropertySource(properties = {
    "cas.authn.spnego.system.kerberos-conf=classpath:kerb5.conf",
    "cas.authn.spnego.system.login-conf=classpath:jaas.conf"
})
class SpengoWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        var state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_EVALUATE_SPNEGO_CLIENT);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_SPNEGO);
        assertNotNull(state);
        state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_START_SPNEGO_AUTHENTICATE);
        assertNotNull(state);
    }
}
