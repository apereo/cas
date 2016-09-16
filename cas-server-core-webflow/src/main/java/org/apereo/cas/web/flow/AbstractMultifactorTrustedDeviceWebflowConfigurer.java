package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;

import java.util.Arrays;

/**
 * This is {@link AbstractMultifactorTrustedDeviceWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class AbstractMultifactorTrustedDeviceWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private FlowDefinitionRegistry flowDefinitionRegistry;
    private boolean enableDeviceRegistration = true;
    
    /**
     * Register multifactor trusted authentication into webflow.
     */
    protected void registerMultifactorTrustedAuthentication() {
        if (flowDefinitionRegistry.getFlowDefinitionCount() <= 0) {
            throw new IllegalArgumentException("Flow definition registry has no flow definitions");
        }
        logger.debug("Flow definitions found in the registry are {}", flowDefinitionRegistry.getFlowDefinitionIds());
        final String flowId = Arrays.stream(flowDefinitionRegistry.getFlowDefinitionIds()).findFirst().get();
        logger.debug("Processing flow definition {}", flowId);
        
        final Flow flow = (Flow) flowDefinitionRegistry.getFlowDefinition(flowId);

        // Set the verify action
        final ActionState state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        final Transition transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        final String targetStateId = transition.getTargetStateId();
        transition.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE));
        final ActionState verifyAction = createActionState(flow, CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE,
                createEvaluateAction("mfaVerifyTrustAction"));
        createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_NO, targetStateId);

        if (enableDeviceRegistration) {
            // Ask to name the device
            final ActionState submit = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
            final Transition success = (Transition) submit.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            final String successTarget = success.getTargetStateId();
            success.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_REGISTER_DEVICE));
            final ViewState viewRegister = createViewState(flow, CasWebflowConstants.STATE_ID_REGISTER_DEVICE, "casMfaRegisterDeviceView");
            viewRegister.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUBMIT, successTarget));
        }

        //set the trust action
        final EndState endState = (EndState) flow.getState(CasWebflowConstants.STATE_ID_SUCCESS);
        endState.getEntryActionList().add(createEvaluateAction("mfaSetTrustAction"));
    }
    
    public void setFlowDefinitionRegistry(final FlowDefinitionRegistry flowDefinitionRegistry) {
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    public FlowDefinitionRegistry getFlowDefinitionRegistry() {
        return flowDefinitionRegistry;
    }

    public boolean isEnableDeviceRegistration() {
        return enableDeviceRegistration;
    }

    public void setEnableDeviceRegistration(final boolean enableDeviceRegistration) {
        this.enableDeviceRegistration = enableDeviceRegistration;
    }
}
