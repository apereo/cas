package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public GraphicalUserAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry flowDefinitionRegistry,
                                                        final ConfigurableApplicationContext applicationContext,
                                                        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            prependActionsToActionStateExecutionList(flow, state, createEvaluateAction(CasWebflowConstants.ACTION_ID_GUA_PREPARE_LOGIN));

            val transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            val targetStateId = transition.getTargetStateId();
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_GUA_GET_USERID, CasWebflowConstants.STATE_ID_GUA_GET_USERID);

            val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_GUA_GET_USERID, "gua/casGuaGetUserIdView");
            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_GUA_DISPLAY_USER_GFX);

            val viewStateGfx = createViewState(flow, CasWebflowConstants.STATE_ID_GUA_DISPLAY_USER_GFX, "gua/casGuaDisplayUserGraphicsView");
            viewStateGfx.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_GUA_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION));
            createTransitionForState(viewStateGfx, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_ACCEPT_GUA);

            val acceptState = createActionState(flow, CasWebflowConstants.STATE_ID_ACCEPT_GUA,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GUA_ACCEPT_USER));
            createStateDefaultTransition(acceptState, targetStateId);
        }
    }
}
