package org.apereo.cas.web.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;

/**
 * This is {@link AbstractMultifactorTrustedDeviceWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractMultifactorTrustedDeviceWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * Trusted authentication scope attribute.
     **/
    public static final String MFA_TRUSTED_AUTHN_SCOPE_ATTR = "mfaTrustedAuthentication";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMultifactorTrustedDeviceWebflowConfigurer.class);

    private boolean enableDeviceRegistration = true;

    public AbstractMultifactorTrustedDeviceWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                             final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                             final boolean enableDeviceRegistration) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.enableDeviceRegistration = enableDeviceRegistration;
    }

    /**
     * Register multifactor trusted authentication into webflow.
     *
     * @param flowDefinitionRegistry the flow definition registry
     */
    protected void registerMultifactorTrustedAuthentication(final FlowDefinitionRegistry flowDefinitionRegistry) {
        if (flowDefinitionRegistry.getFlowDefinitionCount() <= 0) {
            throw new IllegalArgumentException("Flow definition registry has no flow definitions");
        }
        LOGGER.debug("Flow definitions found in the registry are [{}]", (Object[]) flowDefinitionRegistry.getFlowDefinitionIds());
        final String flowId = Arrays.stream(flowDefinitionRegistry.getFlowDefinitionIds()).findFirst().get();
        LOGGER.debug("Processing flow definition [{}]", flowId);

        final Flow flow = (Flow) flowDefinitionRegistry.getFlowDefinition(flowId);

        // Set the verify action
        final ActionState state = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        final Transition transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        final String targetStateId = transition.getTargetStateId();
        transition.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE));
        final ActionState verifyAction = createActionState(flow,
                CasWebflowConstants.STATE_ID_VERIFY_TRUSTED_DEVICE,
                createEvaluateAction("mfaVerifyTrustAction"));

        // handle device registration
        if (enableDeviceRegistration) {
            createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_YES, "finishMfaTrustedAuth");
        } else {
            createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_YES, CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        }
        createTransitionForState(verifyAction, CasWebflowConstants.TRANSITION_ID_NO, targetStateId);

        createDecisionState(flow, CasWebflowConstants.DECISION_STATE_REQUIRE_REGISTRATION,
                isDeviceRegistrationRequired(),
                CasWebflowConstants.VIEW_ID_REGISTER_DEVICE, CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        
        final ActionState submit = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        final Transition success = (Transition) submit.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
        if (enableDeviceRegistration) {
            success.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.VIEW_ID_REGISTER_DEVICE));
        } else {
            success.setTargetStateResolver(new DefaultTargetStateResolver(CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE));
        }
        final ViewState viewRegister = createViewState(flow, CasWebflowConstants.VIEW_ID_REGISTER_DEVICE, "casMfaRegisterDeviceView");
        viewRegister.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE));

        final ActionState registerAction = createActionState(flow, CasWebflowConstants.STATE_ID_REGISTER_TRUSTED_DEVICE, 
                createEvaluateAction("mfaSetTrustAction"));
        createStateDefaultTransition(registerAction, CasWebflowConstants.STATE_ID_SUCCESS);

        if (submit.getActionList().size() == 0) {
            throw new IllegalArgumentException("There are no actions defined for the final submission event of " + flowId);
        }
        final Action act = submit.getActionList().iterator().next();
        final ActionState finishMfaTrustedAuth = createActionState(flow, "finishMfaTrustedAuth", act);
        finishMfaTrustedAuth.getTransitionSet().add(
                createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SUCCESS));
        createStateDefaultTransition(finishMfaTrustedAuth, CasWebflowConstants.STATE_ID_SUCCESS);
    }

    public boolean isEnableDeviceRegistration() {
        return enableDeviceRegistration;
    }

    private static String isDeviceRegistrationRequired() {
        return "flashScope.".concat(MFA_TRUSTED_AUTHN_SCOPE_ATTR).concat("== null");
    }
}
