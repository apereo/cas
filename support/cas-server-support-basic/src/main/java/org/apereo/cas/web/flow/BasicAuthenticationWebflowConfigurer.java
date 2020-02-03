package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link BasicAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class BasicAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    static final String STATE_ID_BASIC_AUTHENTICATION_CHECK = "basicAuthenticationCheck";

    public BasicAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                final ConfigurableApplicationContext applicationContext,
                                                final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(flow, STATE_ID_BASIC_AUTHENTICATION_CHECK, createEvaluateAction("basicAuthenticationAction"));
            val transitionSet = actionState.getTransitionSet();
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.TRANSITION_ID_WARN));
            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));

            createStateDefaultTransition(actionState, getStartState(flow).getId());
            setStartState(flow, actionState);
        }
    }
}
