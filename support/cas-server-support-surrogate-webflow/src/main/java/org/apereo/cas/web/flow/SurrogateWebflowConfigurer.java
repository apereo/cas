package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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

    private final Action selectSurrogateAction;

    public SurrogateWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final Action selectSurrogateAction, final ApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.selectSurrogateAction = selectSurrogateAction;
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createSurrogateListViewState(flow);
            createSurrogateSelectionActionState(flow);
            createTransitionToInjectSurrogateIntoFlow(flow);
            createSurrogateAuthorizationActionState(flow);
        }
    }

    private void createSurrogateAuthorizationActionState(final Flow flow) {
        final ActionState actionState = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        actionState.getEntryActionList().add(createEvaluateAction("surrogateAuthorizationCheck"));
    }

    private void createTransitionToInjectSurrogateIntoFlow(final Flow flow) {
        final ActionState actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        createTransitionForState(actionState, VIEW_ID_SURROGATE_VIEW, VIEW_ID_SURROGATE_VIEW, true);
    }

    private void createSurrogateSelectionActionState(final Flow flow) {
        final ActionState selectSurrogate = createActionState(flow, "selectSurrogate", selectSurrogateAction);
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_REAL_SUBMIT);
    }

    private void createSurrogateListViewState(final Flow flow) {
        final ViewState viewState = createViewState(flow, VIEW_ID_SURROGATE_VIEW, "casSurrogateAuthnListView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "selectSurrogate");
    }
}
