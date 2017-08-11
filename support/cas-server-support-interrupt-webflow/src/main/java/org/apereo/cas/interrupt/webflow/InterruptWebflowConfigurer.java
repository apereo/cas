package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link InterruptWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String INTERRUPT_VIEW_ID = "interruptView";
    private static final String STATE_ID_INQUIRE_INTERRUPT_CHECK = "inquireInterruptCheck";
    
    public InterruptWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, flowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createInquireActionState(flow);
            createAcceptableUsagePolicyView(flow);
        }
    }

    private void createAcceptableUsagePolicyView(final Flow flow) {
        final ViewState viewState = createViewState(flow, INTERRUPT_VIEW_ID, "casInterruptView");
        viewState.getEntryActionList().add(createEvaluateAction("prepareInterruptViewAction"));
    }
    
    private void createInquireActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, STATE_ID_INQUIRE_INTERRUPT_CHECK, createEvaluateAction("inquireInterruptAction"));
        
        final Transition noInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_NO,
                CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
        actionState.getTransitionSet().add(noInterruptTransition);
        
        final Transition yesInterruptTransition = createTransition(CasWebflowConstants.TRANSITION_ID_YES, INTERRUPT_VIEW_ID);
        actionState.getTransitionSet().add(yesInterruptTransition);
    }
}
