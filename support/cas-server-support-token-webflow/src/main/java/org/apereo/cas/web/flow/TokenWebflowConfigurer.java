package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link TokenWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for token authentication support integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TokenWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String STATE_ID_TOKEN_AUTHENTICATION_CHECK = "tokenAuthenticationCheck";

    private static final String ACTION_ID_TOKEN_AUTHENTICATION_ACTION = "tokenAuthenticationAction";

    public TokenWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  final ConfigurableApplicationContext applicationContext,
                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getToken().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(flow, STATE_ID_TOKEN_AUTHENTICATION_CHECK,
                createEvaluateAction(ACTION_ID_TOKEN_AUTHENTICATION_ACTION));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
            createStateDefaultTransition(actionState, getStartState(flow).getId());
            setStartState(flow, actionState);
        }
    }
}
