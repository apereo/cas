package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.ViewState;
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

    private ViewState insertViewState(final Flow flow, final String defaultStateId, final ActionState initState) {
        if (casProperties.getAuthn().getGua().isGraphicOnMainLogin()) {
            return getState(flow, defaultStateId, ViewState.class);
        }
        createTransitionForState(initState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                STATE_ID_GUA_DISPLAY_USER_GFX, true);
        val viewState = createViewState(flow, STATE_ID_GUA_DISPLAY_USER_GFX,
                "casGuaDisplayUserGraphicsView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT,
                defaultStateId);
        /*
        createTransitionForState(viewState,
                CasWebflowConstants.TRANSITION_ID_SUBMIT,
                CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID);
                */
        return viewState;
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_MULTIPHASE_STORE_USERID, ActionState.class);
            val transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val targetStateId = transition.getTargetStateId();

            val viewStateGfx = insertViewState(flow, targetStateId, state);

            viewStateGfx.getRenderActionList().add(createEvaluateAction(ACTION_ID_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION));

            /*
            val acceptState = createActionState(flow, STATE_ID_ACCEPT_GUA,
                createEvaluateAction(ACTION_ID_ACCEPT_USER_GRAPHICS_FOR_AUTHENTICATION));
            createStateDefaultTransition(acceptState, targetStateId);
            */
        }
    }
}
