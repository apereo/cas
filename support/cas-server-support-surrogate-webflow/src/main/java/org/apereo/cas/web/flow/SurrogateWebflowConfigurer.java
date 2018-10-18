package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SurrogateWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogateWebflowConfigurer extends AbstractCasWebflowConfigurer {
    /**
     * The view id 'surrogateListView'.
     */
    public static final String TRANSITION_ID_SURROGATE_VIEW = "surrogateListView";

    /**
     * Skip surrogate view if no surrogates can be found.
     */
    public static final String TRANSITION_ID_SKIP_SURROGATE = "skipSurrogateView";

    public SurrogateWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final ApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createSurrogateListViewState(flow);
            createSurrogateSelectionActionState(flow);
            createSurrogateAuthorizationActionState(flow);

            createTransitionToInjectSurrogateIntoFlow(flow);
        }
    }

    private void createSurrogateAuthorizationActionState(final Flow flow) {
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        actionState.getEntryActionList().add(createEvaluateAction("surrogateAuthorizationCheck"));
    }

    private void createTransitionToInjectSurrogateIntoFlow(final Flow flow) {
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);

        LOGGER.debug("Locating transition id [{}] to for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS, actionState.getId());
        val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        val loadSurrogatesAction = createActionState(flow, "loadSurrogatesAction", "loadSurrogatesListAction");
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
        createTransitionForState(loadSurrogatesAction, TRANSITION_ID_SURROGATE_VIEW, TRANSITION_ID_SURROGATE_VIEW);
        createTransitionForState(loadSurrogatesAction, TRANSITION_ID_SKIP_SURROGATE, targetSuccessId);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, loadSurrogatesAction.getId(), true);

    }

    private void createSurrogateSelectionActionState(final Flow flow) {
        val selectSurrogate = createActionState(flow, "selectSurrogate", "selectSurrogateAction");
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
    }

    private void createSurrogateListViewState(final Flow flow) {
        val viewState = createViewState(flow, TRANSITION_ID_SURROGATE_VIEW, "casSurrogateAuthnListView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "selectSurrogate");
    }
}
