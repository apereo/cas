package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
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
        createViewState(flow, VIEW_ID_CONSENT_VIEW, VIEW_ID_CONSENT_VIEW);
    }

    private void createConsentTransitions(final Flow flow) {
        final ActionState sendTicket = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        createTransitionForState(sendTicket, CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, VIEW_ID_CONSENT_VIEW);
    }

    private void createConsentRequiredCheckAction(final Flow flow) {
        final ActionState sendTicket = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        final List<Action> actions = StreamSupport.stream(sendTicket.getActionList().spliterator(), false)
                .collect(Collectors.toList());
        actions.add(0, createEvaluateAction("checkConsentRequiredAction"));
        sendTicket.getActionList().forEach(a -> sendTicket.getActionList().remove(a));
        actions.forEach(sendTicket.getActionList()::add);
    }
}
