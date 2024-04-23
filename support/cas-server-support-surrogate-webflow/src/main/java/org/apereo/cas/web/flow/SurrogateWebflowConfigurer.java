package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
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

    public SurrogateWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final ConfigurableApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createSurrogateAuthenticationActionState(flow);
            createSurrogateListViewState(flow);
            createSurrogateSelectionActionState(flow);
            createSurrogateAuthorizationActionState(flow);
            createTransitionToInjectSurrogateIntoFlow(flow);
        }
    }

    private void createSurrogateAuthenticationActionState(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        prependActionsToActionStateExecutionList(flow, state,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION));
    }

    /**
     * Create surrogate list view state.
     *
     * @param flow the flow
     */
    protected void createSurrogateListViewState(final Flow flow) {
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_SURROGATE_VIEW, "surrogate/casSurrogateAuthnListView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_SELECT_SURROGATE);

        createEndState(flow, CasWebflowConstants.STATE_ID_SURROGATE_WILDCARD_VIEW, "surrogate/casSurrogateAuthnWildcardView");
    }
    
    private void createSurrogateAuthorizationActionState(final Flow flow) {
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        actionState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SURROGATE_AUTHORIZATION_CHECK));
    }

    private void createTransitionToInjectSurrogateIntoFlow(final Flow flow) {
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);

        LOGGER.debug("Locating transition id [{}] to for state [{}", CasWebflowConstants.TRANSITION_ID_SUCCESS, actionState.getId());
        val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        val loadSurrogatesAction = createActionState(flow, CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION,
            CasWebflowConstants.ACTION_ID_LOAD_SURROGATES_LIST_ACTION);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SURROGATE_VIEW, CasWebflowConstants.STATE_ID_SURROGATE_VIEW);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SURROGATE_WILDCARD_VIEW, CasWebflowConstants.STATE_ID_SURROGATE_WILDCARD_VIEW);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_SKIP_SURROGATE, targetSuccessId);
        createTransitionForState(loadSurrogatesAction, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, loadSurrogatesAction.getId(), true);

    }

    private void createSurrogateSelectionActionState(final Flow flow) {
        val selectSurrogate = createActionState(flow, CasWebflowConstants.STATE_ID_SELECT_SURROGATE,
            CasWebflowConstants.ACTION_ID_SELECT_SURROGATE_ACTION);
        val actionState = getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
        val targetSuccessId = actionState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_SUCCESS, targetSuccessId);
        createTransitionForState(selectSurrogate, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_SURROGATE_VIEW);
    }
}
