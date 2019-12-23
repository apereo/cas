package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link MultiphaseAuthenticationWebflowConfigurer}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@Slf4j
public class MultiphaseAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
	/**
	 * Transition to obtain username.
	 */
	public static final String TRANSITION_ID_MULTIPHASE_GET_USERID = "multiphaseGetUserId";

	static final String STATE_ID_MULTIPHASE_GET_USERID = "multiphaseGetUserIdView";
	static final String STATE_ID_STORE_USERID = "storeUserIdForAuthentication";
	static final String ACTION_ID_STORE_USERID_FOR_AUTHENTICATION = "storeUserIdForAuthenticationAction";

	public MultiphaseAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                        final ApplicationContext applicationContext,
                                                        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

	@Override
	protected void doInitialize() {
		val flow = getLoginFlow();
		if (flow != null) {
            LOGGER.debug("Current state ids: [{}]", flow.getStateIds());
            LOGGER.debug("Configuring multiphase webflow");
            // init login state form
			val initState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            LOGGER.debug("Locating transition id [{}] for state [{}]", 
                    CasWebflowConstants.TRANSITION_ID_SUCCESS, initState.getId());
            // transition object with id of success from init state
			val initTransition = (Transition) initState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            // target following that transition (maybe main page; could be
            // otherwise)
			val targetStateId = initTransition.getTargetStateId();
            // add a transition from init state on get_userid transition to
            // get_userid state
            LOGGER.debug("Creating transition with id [{}] for state [{}] to state [{}]",
                    TRANSITION_ID_MULTIPHASE_GET_USERID, initState.getId(), STATE_ID_MULTIPHASE_GET_USERID);
			createTransitionForState(initState, TRANSITION_ID_MULTIPHASE_GET_USERID, STATE_ID_MULTIPHASE_GET_USERID);

			val getUserIdState = createViewState(flow, STATE_ID_MULTIPHASE_GET_USERID, "casMultiphaseGetUserIdView");
            LOGGER.debug("Creating transition with id [{}] for state [{}] to state [{}]",
                    CasWebflowConstants.TRANSITION_ID_SUBMIT, getUserIdState.getId(), STATE_ID_STORE_USERID);
			createTransitionForState(getUserIdState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_STORE_USERID);

			val actionState = createActionState(flow, STATE_ID_STORE_USERID,
                    createEvaluateAction(ACTION_ID_STORE_USERID_FOR_AUTHENTICATION));
			createStateDefaultTransition(actionState, targetStateId);
		}
	}
}
