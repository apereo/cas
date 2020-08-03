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

    static final String STATE_ID_WS_FEDERATION_ACTION = "wsFederationAction";
    private static final String WS_FEDERATION_REDIRECT = "wsFederationRedirect";

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

            val actionState = createActionState(flow, STATE_ID_WS_FEDERATION_ACTION, createEvaluateAction(STATE_ID_WS_FEDERATION_ACTION));
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_REDIRECT, WS_FEDERATION_REDIRECT);

            val currentStartState = getStartState(flow).getId();
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_PROCEED, currentStartState);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
            setStartState(flow, actionState);
        }
    }

    private void createStopWebflowViewState(final Flow flow) {
        createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.STATE_ID_WSFED_STOP_WEBFLOW);
    }
}
