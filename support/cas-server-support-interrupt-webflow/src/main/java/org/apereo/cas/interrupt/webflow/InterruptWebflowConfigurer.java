package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.interrupt.InterruptCoreProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
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
    public InterruptWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry flowDefinitionRegistry,
                                      final ConfigurableApplicationContext applicationContext,
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
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PROCEED,
            CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW, true);
    }

    private void createTransitionStateToInterrupt(final Flow flow) {
        val submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION, true);

        val triggerMode = casProperties.getInterrupt().getCore().getTriggerMode();
        if (triggerMode == InterruptCoreProperties.InterruptTriggerModes.AFTER_AUTHENTICATION) {
            val ticketCreateState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            prependActionsToActionStateExecutionList(flow, ticketCreateState, getInquireInterruptAction());
            createTransitionForState(ticketCreateState, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED,
                CasWebflowConstants.STATE_ID_INTERRUPT_VIEW);
        }

        prependActionsToActionStateExecutionList(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
            CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION);
        createTransitionForState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
            CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, CasWebflowConstants.STATE_ID_INTERRUPT_VIEW);
    }

    private void createTransitionStateForMultifactorSubflows(final Flow flow) {
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        providerMap.forEach((k, v) -> {
            if (containsSubflowState(flow, v.getId())) {
                val state = getState(flow, v.getId(), SubflowState.class);
                createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION, true);
            }
        });
    }

    private ActionState getRealSubmissionState(final Flow flow) {
        val triggerMode = casProperties.getInterrupt().getCore().getTriggerMode();
        if (triggerMode == InterruptCoreProperties.InterruptTriggerModes.AFTER_SSO) {
            return getState(flow, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET, ActionState.class);
        }
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    private void createInterruptView(final Flow flow) {
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_INTERRUPT_VIEW, "interrupt/casInterruptView");
        viewState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PREPARE_INTERRUPT_VIEW));
        createStateDefaultTransition(viewState, CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT_ACTION);

        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        val finalizeInterrupt = createActionState(flow, CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT_ACTION,
            CasWebflowConstants.ACTION_ID_FINALIZE_INTERRUPT);
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.TRANSITION_ID_SUCCESS, target);
        createTransitionForState(finalizeInterrupt, CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT);
        createEndState(flow, CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT);
    }

    private void createInquireActionState(final Flow flow) {
        val inquireState = createActionState(flow, CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION, getInquireInterruptAction());
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(inquireState, CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, target);
        createTransitionForState(inquireState, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, CasWebflowConstants.STATE_ID_INTERRUPT_VIEW);
    }

    private EvaluateAction getInquireInterruptAction() {
        return createEvaluateAction(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION);
    }

}
