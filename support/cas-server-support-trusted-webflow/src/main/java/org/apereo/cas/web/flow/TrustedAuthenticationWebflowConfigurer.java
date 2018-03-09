package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link TrustedAuthenticationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for trusted authn integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TrustedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public TrustedAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                  final ApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final EvaluateAction action = createEvaluateAction("remoteUserAuthenticationAction");
            final ActionState actionState = createActionState(flow, "remoteAuthenticate", action);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
            final String currentStartState = getStartState(flow).getId();
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_ERROR, currentStartState);
            createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
            setStartState(flow, actionState);
        }
    }
}
