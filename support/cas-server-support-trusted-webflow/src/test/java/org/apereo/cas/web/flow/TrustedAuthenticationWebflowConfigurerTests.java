package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.TrustedAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.web.flow.config.TrustedAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TrustedAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    TrustedAuthenticationWebflowConfiguration.class,
    TrustedAuthenticationComponentSerializationConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    BaseWebflowConfigurerTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class TrustedAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    public void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(TrustedAuthenticationWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.ACTION_ID_REMOTE_TRUSTED_AUTHENTICATION);
        assertNotNull(state);
    }
}
