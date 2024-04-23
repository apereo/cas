package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public WsFederationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                         final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                         final ConfigurableApplicationContext applicationContext,
                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createStopWebflowViewState(flow);

            val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_WS_FEDERATION_START, CasWebflowConstants.ACTION_ID_WS_FEDERATION);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_REDIRECT, CasWebflowConstants.STATE_ID_WS_FEDERATION_REDIRECT);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET,
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);

            val currentStartState = getStartState(flow).getId();
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_PROCEED, currentStartState);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
            setStartState(flow, actionState);

            val initLogin = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
            initLogin.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_WS_FEDERATION_REDIRECT));
        }
    }

    private void createStopWebflowViewState(final Flow flow) {
        createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, "wsfed/casWsFedStopWebflow");
    }
}
