package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
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

    static final String STATE_ID_SURROGATE_VIEW = "surrogateListView";

    static final String STATE_ID_LOAD_SURROGATES_ACTION = "loadSurrogatesAction";

    static final String STATE_ID_SELECT_SURROGATE = "selectSurrogate";

    private static final String VIEW_ID_CAS_SURROGATE_AUTHN_LIST_VIEW = "casSurrogateAuthnListView";

    private static final String ACTION_ID_LOAD_SURROGATES_LIST_ACTION = "loadSurrogatesListAction";

    private static final String ACTION_ID_SELECT_SURROGATE_ACTION = "selectSurrogateAction";

    public SurrogateWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final ConfigurableApplicationContext applicationContext,
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

        val loadSurrogatesAction = createActionState(flow, STATE_ID_LOAD_SURROGATES_ACTION, ACTION_ID_LOAD_SURROGATES_LIST_ACTION);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
        createTransitionForState(loadSurrogatesAction, TRANSITION_ID_SURROGATE_VIEW, STATE_ID_SURROGATE_VIEW);
        createTransitionForState(loadSurrogatesAction, TRANSITION_ID_SKIP_SURROGATE, targetSuccessId);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, loadSurrogatesAction.getId(), true);

    }

    private void createSurrogateSelectionActionState(final Flow flow) {
        val selectSurrogate = createActionState(flow, STATE_ID_SELECT_SURROGATE, ACTION_ID_SELECT_SURROGATE_ACTION);
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_ERROR, STATE_ID_SURROGATE_VIEW);
    }

    private void createSurrogateListViewState(final Flow flow) {
        val viewState = createViewState(flow, STATE_ID_SURROGATE_VIEW, VIEW_ID_CAS_SURROGATE_AUTHN_LIST_VIEW);
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, "selectSurrogate");
    }
}
