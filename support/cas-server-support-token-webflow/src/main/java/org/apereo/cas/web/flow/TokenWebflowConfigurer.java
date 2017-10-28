package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link TokenWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for token authentication support integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */

public class TokenWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public TokenWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  final ApplicationContext applicationContext,
                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState actionState = createActionState(flow, "tokenAuthenticationCheck",
                    createEvaluateAction("tokenAuthenticationAction"));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
            createStateDefaultTransition(actionState, getStartState(flow).getId());
            setStartState(flow, actionState);
        }
    }
}
