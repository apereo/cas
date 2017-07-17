package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link ConsentWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConsentWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private static final String VIEW_ID_CONSENT_VIEW = "casConsentView";
    private static final String STATE_ID_CONSENT_CONFIRM = "confirmAttributeConsent";
    private static final String ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT = "generateServiceTicketAfterConsent";

    public ConsentWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createConsentRequiredCheckAction(flow);
            createConsentTransitions(flow);
            createConsentView(flow);
        }
    }

    private void createConsentView(final Flow flow) {
        final ViewState state = createViewState(flow, VIEW_ID_CONSENT_VIEW, VIEW_ID_CONSENT_VIEW);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CONFIRM, STATE_ID_CONSENT_CONFIRM);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

        final ActionState action = createActionState(flow, STATE_ID_CONSENT_CONFIRM, createEvaluateAction("confirmConsentAction"));
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT);
    }

    private void createConsentTransitions(final Flow flow) {
        final ActionState sendTicket = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        createTransitionForState(sendTicket, CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, VIEW_ID_CONSENT_VIEW);
    }

    private void createConsentRequiredCheckAction(final Flow flow) {
        final ActionState sendTicket = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        final List<Action> actions = StreamSupport.stream(sendTicket.getActionList().spliterator(), false).collect(Collectors.toList());
        actions.add(0, createEvaluateAction("checkConsentRequiredAction"));
        sendTicket.getActionList().forEach(a -> sendTicket.getActionList().remove(a));
        actions.forEach(sendTicket.getActionList()::add);

        final ActionState generateServiceTicket = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        final ActionState consentTicketAction = createActionState(flow, ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT);
        cloneActionState(generateServiceTicket, consentTicketAction);
    }
}
