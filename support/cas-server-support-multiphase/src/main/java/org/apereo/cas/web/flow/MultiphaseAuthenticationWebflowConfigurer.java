package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
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
			val initState = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
			val initTransition = (Transition) initState.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
			val targetStateId = initTransition.getTargetStateId();
			createTransitionForState(initState, TRANSITION_ID_MULTIPHASE_GET_USERID, STATE_ID_MULTIPHASE_GET_USERID);

			val getUserIdState = createViewState(flow, STATE_ID_MULTIPHASE_GET_USERID, "casMultiphaseGetUserIdView");
			createTransitionForState(getUserIdState, CasWebflowConstants.TRANSITION_ID_SUBMIT, 
					ACTION_ID_STORE_USERID_FOR_AUTHENTICATION);

			val actionState = createActionState(flow, STATE_ID_STORE_USERID,
					createEvaluateAction(ACTION_ID_STORE_USERID_FOR_AUTHENTICATION));
			createStateDefaultTransition(actionState, targetStateId);
		}
	}
}
