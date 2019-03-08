package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.SubflowState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link InterruptWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private static final String INTERRUPT_VIEW_ID = "casInterruptView";

    private static final String VIEW_ID_INTERRUPT_VIEW = "interruptView";

    private static final String STATE_ID_INQUIRE_INTERRUPT_ACTION = "inquireInterruptAction";
    private static final String STATE_ID_FINALIZE_INTERRUPT_ACTION = "finalizeInterruptFlowAction";
    private static final String STATE_ID_PREPARE_INTERRUPT_VIEW_ACTION = "prepareInterruptViewAction";
    private static final String STATE_ID_FINISHED_INTERRUPT = "finishedInterrupt";

    public InterruptWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry flowDefinitionRegistry,
                                      final ApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();

        if (flow != null) {
            createInquireActionState(flow);
            createInterruptView(flow);
            createTransitionStateToInterrupt(flow);
            createTransitionStateForMultifactorSubflows(flow);
            createTransitionStateForAuthenticationWarnings(flow);
        }
    }

    private void createTransitionStateForAuthenticationWarnings(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS, ViewState.class);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PROCEED, STATE_ID_INQUIRE_INTERRUPT_ACTION, true);
    }

    private void createTransitionStateToInterrupt(final Flow flow) {
        val submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_INQUIRE_INTERRUPT_ACTION, true);

        val ticketCreateState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
        prependActionsToActionStateExecutionList(flow, ticketCreateState, getInquireInterruptAction());
        createTransitionForState(ticketCreateState, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, VIEW_ID_INTERRUPT_VIEW);
    }

    private void createTransitionStateForMultifactorSubflows(final Flow flow) {
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        providerMap.forEach((k, v) -> {
            if (containsSubflowState(flow, v.getId())) {
                val state = getState(flow, v.getId(), SubflowState.class);
                createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_INQUIRE_INTERRUPT_ACTION, true);
            }
        });
    }

    private ActionState getRealSubmissionState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    private void createInterruptView(final Flow flow) {
        val viewState = createViewState(flow, VIEW_ID_INTERRUPT_VIEW, INTERRUPT_VIEW_ID);
        viewState.getEntryActionList().add(createEvaluateAction(STATE_ID_PREPARE_INTERRUPT_VIEW_ACTION));
        createStateDefaultTransition(viewState, STATE_ID_FINALIZE_INTERRUPT_ACTION);

        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        val finalizeInterrupt = createActionState(flow, STATE_ID_FINALIZE_INTERRUPT_ACTION,
            createEvaluateAction(STATE_ID_FINALIZE_INTERRUPT_ACTION));
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.TRANSITION_ID_SUCCESS, target);
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, STATE_ID_FINISHED_INTERRUPT);
        createEndState(flow, STATE_ID_FINISHED_INTERRUPT);
    }

    private void createInquireActionState(final Flow flow) {
        val inquireState = createActionState(flow, STATE_ID_INQUIRE_INTERRUPT_ACTION, getInquireInterruptAction());

        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        val noInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, target);
        val transitionSet = inquireState.getTransitionSet();
        transitionSet.add(noInterruptTransition);

        val yesInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, VIEW_ID_INTERRUPT_VIEW);
        transitionSet.add(yesInterruptTransition);
    }

    private EvaluateAction getInquireInterruptAction() {
        return createEvaluateAction(STATE_ID_INQUIRE_INTERRUPT_ACTION);
    }
}
