package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.ViewState;
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

    static final String ACTION_ID_STORE_USERID_FOR_AUTHENTICATION = "storeUserIdForAuthenticationAction";

    public MultiphaseAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                     final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                     final ConfigurableApplicationContext applicationContext,
                                                     final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val viewStateLogin = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            LOGGER.debug("Locating transition id [{}] for state [{}]", 
                    CasWebflowConstants.TRANSITION_ID_SUBMIT, viewStateLogin.getId());
            val initTransition = (Transition) viewStateLogin.getTransition(CasWebflowConstants.TRANSITION_ID_SUBMIT);
            val targetStateId = initTransition.getTargetStateId();

            createTransitionForState(viewStateLogin, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                    CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID, true);
            
            val storeUserId = createActionState(flow, CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID,
                    createEvaluateAction(ACTION_ID_STORE_USERID_FOR_AUTHENTICATION));

            createTransitionForState(storeUserId, CasWebflowConstants.TRANSITION_ID_SUCCESS, 
                    viewStateLogin.getId());
            createTransitionForState(storeUserId, CasWebflowConstants.TRANSITION_ID_PROCEED, targetStateId);
        }
    }
}
