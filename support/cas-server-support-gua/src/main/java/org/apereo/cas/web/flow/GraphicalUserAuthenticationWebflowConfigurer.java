package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
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
    /**
     * Transition to obtain username.
     */
    public static final String TRANSITION_ID_GUA_GET_USERID = "guaGetUserId";

    private static final String STATE_ID_ACCEPT_GUA = "acceptUserGraphicsForAuthentication";
    private static final String STATE_ID_GUA_GET_USERID = "guaGetUserIdView";
    private static final String STATE_ID_GUA_DISPLAY_USER_GFX = "guaDisplayUserGraphics";

    public GraphicalUserAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                        final ApplicationContext applicationContext,
                                                        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            final Transition transition = (Transition) state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            final String targetStateId = transition.getTargetStateId();

            createTransitionForState(state, TRANSITION_ID_GUA_GET_USERID, STATE_ID_GUA_GET_USERID);
            final ViewState viewState = createViewState(flow, STATE_ID_GUA_GET_USERID, "casGuaGetUserIdView");
            createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_GUA_DISPLAY_USER_GFX);

            final ViewState viewStateGfx = createViewState(flow, STATE_ID_GUA_DISPLAY_USER_GFX, "casGuaDisplayUserGraphicsView");
            viewStateGfx.getRenderActionList().add(createEvaluateAction("displayUserGraphicsBeforeAuthenticationAction"));
            createTransitionForState(viewStateGfx, CasWebflowConstants.TRANSITION_ID_SUBMIT, STATE_ID_ACCEPT_GUA);

            final ActionState acceptState = createActionState(flow, STATE_ID_ACCEPT_GUA,
                    createEvaluateAction("acceptUserGraphicsForAuthenticationAction"));
            createStateDefaultTransition(acceptState, targetStateId);
        }
    }
}
