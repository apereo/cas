package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link RemoteAddressWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for remote address integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class RemoteAddressWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String START_AUTHENTICATE = "startAuthenticate";

    public RemoteAddressWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                          final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                          final ConfigurableApplicationContext applicationContext,
                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(flow, START_AUTHENTICATE, "remoteAddressCheck");
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
        }
    }
}
