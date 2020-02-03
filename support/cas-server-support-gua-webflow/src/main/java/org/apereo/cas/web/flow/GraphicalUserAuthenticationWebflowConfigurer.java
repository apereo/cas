package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String STATE_ID_ACCEPT_GUA = "acceptUserGraphicsForAuthentication";
    static final String STATE_ID_GUA_DISPLAY_USER_GFX = "guaDisplayUserGraphics";
    static final String ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION = "displayUserGraphicsBeforeAuthenticationAction";

    static final String ACTION_ID_ACCEPT_USER_GRAPHICS_FOR_AUTHENTICATION = "acceptUserGraphicsForAuthenticationAction";

    public GraphicalUserAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                        final ConfigurableApplicationContext applicationContext,
                                                        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID, ActionState.class);
            val transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val targetStateId = transition.getTargetStateId();

            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    STATE_ID_GUA_DISPLAY_USER_GFX, true);

            val skipInterstitial = casProperties.getAuth().getGua().isGraphicOnMainLogin();

            var viewStateGfx;

            if (casProperties.getAuth().getGua().isGraphicOnMainLogin()) {
                viewStateGfx = getState(flow, targetStateId, ViewState.class);
            } else {
                viewStateGfx = createViewState(flow, STATE_ID_GUA_DISPLAY_USER_GFX, 
                        "casGuaDisplayUserGraphicsView");
                createTransitionForState(viewStateGfx, 
                        CasWebflowConstants.TRANSITION_ID_SUBMIT, 
                        CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID);
            }

            viewStateGfx.getRenderActionList().add(createEvaluateAction(ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION));

            /*
            val acceptState = createActionState(flow, STATE_ID_ACCEPT_GUA,
                createEvaluateAction(ACTION_ID_ACCEPT_USER_GRAPHICS_FOR_AUTHENTICATION));
            createStateDefaultTransition(acceptState, targetStateId);
            */
        }
    }
}
