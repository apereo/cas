package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCaptchaAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCaptchaWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(CasCaptchaAutoConfiguration.class)
@Tag("WebflowConfig")
class CasCaptchaWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() throws Throwable {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        assertTrue(Arrays.stream(state.getActionList().toArray())
            .filter(EvaluateAction.class::isInstance)
            .map(EvaluateAction.class::cast)
            .anyMatch(r -> r.toString().contains(CasWebflowConstants.ACTION_ID_VALIDATE_CAPTCHA)));
    }
}
