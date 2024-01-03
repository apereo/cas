package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.TokenAuthenticationAutoConfiguration;
import org.apereo.cas.config.TokenAuthenticationWebflowAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import({
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    TokenAuthenticationAutoConfiguration.class,
    TokenAuthenticationWebflowAutoConfiguration.class
})
@Tag("WebflowConfig")
class TokenWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (TransitionableState) flow.getState(CasWebflowConstants.STATE_ID_TOKEN_AUTHENTICATION_CHECK);
        assertNotNull(state);
    }
}
