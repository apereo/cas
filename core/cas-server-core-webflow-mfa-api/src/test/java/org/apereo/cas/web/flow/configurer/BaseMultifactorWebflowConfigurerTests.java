package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.TransitionableState;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseMultifactorWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @author Hayden Sartoris
 * @since 6.2.0
 */
public abstract class BaseMultifactorWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowExecutionPlan.BEAN_NAME)
    protected CasWebflowExecutionPlan casWebflowExecutionPlan;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
    protected FlowDefinitionRegistry flowDefinitionRegistry;

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void setup() {
        casWebflowExecutionPlan.execute();
    }

    /**
     * Ensures that, for every transition within this MFA flow, the target
     * state is present within the flow.
     */
    @Test
    void ensureAllTransitionDestinationsExistInFlow() {
        val registry = getMultifactorFlowDefinitionRegistry();
        assertTrue(registry.containsFlowDefinition(getMultifactorEventId()));
        val flow = (Flow) registry.getFlowDefinition(getMultifactorEventId());
        val states = Arrays.asList(flow.getStateIds());
        states.forEach(stateId -> {
            val state = (State) flow.getState(stateId);
            if (state instanceof TransitionableState) {
                ((TransitionableState) state).getTransitionSet().forEach(t -> assertTrue(flow.containsState(t.getTargetStateId()),
                    () -> String.format("Destination of transition [%s]-%s->[%s] must be in flow definition",
                        stateId, t.getId(), t.getTargetStateId())));
            }
        });
    }

    @Test
    void verifyOperation() {
        val registry = getMultifactorFlowDefinitionRegistry();
        assertTrue(registry.containsFlowDefinition(getMultifactorEventId()));
        val flow = (Flow) registry.getFlowDefinition(getMultifactorEventId());
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_MFA_CHECK_BYPASS));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_MFA_CHECK_AVAILABLE));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_MFA_FAILURE));
        val loginFlow = (Flow) flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertInstanceOf(SubflowState.class, loginFlow.getState(getMultifactorEventId()));
    }

    @Test
    void verifyTrustedDevice() {
        val registry = getMultifactorFlowDefinitionRegistry();
        assertTrue(registry.containsFlowDefinition(getMultifactorEventId()));
        val flow = (Flow) registry.getFlowDefinition(getMultifactorEventId());
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISH_MFA_TRUSTED_AUTH));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_PREPARE_REGISTER_TRUSTED_DEVICE));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_REGISTER_DEVICE_VIEW));

        val prepare = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_PREPARE_REGISTER_TRUSTED_DEVICE);
        assertNotNull(prepare.getTransition(CasWebflowConstants.TRANSITION_ID_SKIP));
        assertNotNull(prepare.getTransition(CasWebflowConstants.TRANSITION_ID_REGISTER));
        assertNotNull(prepare.getTransition(CasWebflowConstants.TRANSITION_ID_STORE));
    }

    protected abstract FlowDefinitionRegistry getMultifactorFlowDefinitionRegistry();

    protected abstract String getMultifactorEventId();
}
