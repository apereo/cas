package org.apereo.cas.web.flow;

import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SurrogateWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * The view id 'surrogateListView'.
     */
    public static final String VIEW_ID_SURROGATE_VIEW = "surrogateListView";

    public SurrogateWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ViewState viewState = createViewState(flow, VIEW_ID_SURROGATE_VIEW, "casSurrogateAuthnListView");
            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, 
                    CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
            final ActionState actionState = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
            createTransitionForState(actionState, VIEW_ID_SURROGATE_VIEW, VIEW_ID_SURROGATE_VIEW, true);
        }
    }
}
