package org.apereo.cas.web.flow.configurer;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link DefaultLogoutWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultLogoutWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public DefaultLogoutWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                          final FlowDefinitionRegistry flowDefinitionRegistry,
                                          final ApplicationContext applicationContext,
                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLogoutFlow();

        if (flow != null) {
            createLogoutConfirmationView(flow);
            createDoLogoutActionState(flow);
            createFrontLogoutActionState(flow);
        }
    }

    /**
     * Create front logout action state.
     *
     * @param flow the flow
     */
    protected void createFrontLogoutActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, CasWebflowConstants.STATE_ID_FRONT_LOGOUT,
            createEvaluateAction("frontChannelLogoutAction"));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_PROPAGATE, "propagateLogoutRequests");
    }

    private void createDoLogoutActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, CasWebflowConstants.STATE_ID_DO_LOGOUT, createEvaluateAction("logoutAction"));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, "front", CasWebflowConstants.STATE_ID_FRONT_LOGOUT);
    }

    /**
     * Create logout confirmation view.
     *
     * @param flow the flow
     */
    protected void createLogoutConfirmationView(final Flow flow) {
        final ViewState view = createViewState(flow, CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW, "casConfirmLogoutView");
        createTransitionForState(view, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
    }

}

