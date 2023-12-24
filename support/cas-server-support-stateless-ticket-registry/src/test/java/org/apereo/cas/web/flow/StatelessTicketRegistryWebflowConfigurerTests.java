package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.config.StatelessTicketRegistryConfiguration;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.Expression;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StatelessTicketRegistryWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Import({
    StatelessTicketRegistryConfiguration.class,
    CasWebflowAccountProfileConfiguration.class
})
@Tag("WebflowConfig")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
public class StatelessTicketRegistryWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY)
    private FlowDefinitionRegistry accountProfileFlowRegistry;

    @Test
    void verifyOperation() throws Throwable {
        val flow = (Flow) loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        val state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        assertNotNull(state);
        val action = (EvaluateAction) state.getActionList().get(0);
        var field = ReflectionUtils.findField(EvaluateAction.class, "expression");
        Objects.requireNonNull(field).trySetAccessible();
        var fieldValue = ((Expression) ReflectionUtils.getField(Objects.requireNonNull(field), action)).getExpressionString();
        assertEquals(CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE, fieldValue);

        val accountFlow = accountProfileFlowRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        assertNotNull(flow);
        val tgtCheck = (ActionState) accountFlow.getState(CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK);
        assertNotNull(tgtCheck);
        val firstAction = (EvaluateAction) state.getActionList().get(0);
        field = ReflectionUtils.findField(EvaluateAction.class, "expression");
        Objects.requireNonNull(field).trySetAccessible();
        fieldValue = ((Expression) ReflectionUtils.getField(Objects.requireNonNull(field), firstAction)).getExpressionString();
        assertEquals(CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE, fieldValue);

    }
}
