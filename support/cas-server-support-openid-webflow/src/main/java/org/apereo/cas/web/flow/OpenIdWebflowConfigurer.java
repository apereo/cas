package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link OpenIdWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for openid integration.
 *
 * @author Misagh Moayyed
 * @deprecated 6.2
 * @since 4.2
 */
@Deprecated(since = "6.2.0")
public class OpenIdWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public OpenIdWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                   final ConfigurableApplicationContext applicationContext,
                                   final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    private static String getOpenIdModeCondition() {
        return "externalContext.requestParameterMap['openid.mode'] ne '' "
            + "&& externalContext.requestParameterMap['openid.mode'] ne null "
            + "&& externalContext.requestParameterMap['openid.mode'] ne 'associate'";
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();

        if (flow != null) {
            val condition = getOpenIdModeCondition();

            val decisionState = createDecisionState(flow, CasWebflowConstants.DECISION_STATE_OPEN_ID_SELECT_FIRST_ACTION,
                condition, CasWebflowConstants.STATE_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION,
                getStartState(flow).getId());

            val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION));

            val transitionSet = actionState.getTransitionSet();
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.TRANSITION_ID_WARN));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));

            setStartState(flow, decisionState);
        }
    }
}
