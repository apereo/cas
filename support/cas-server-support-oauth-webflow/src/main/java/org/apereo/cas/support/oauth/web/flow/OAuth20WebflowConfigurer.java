package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link OAuth20WebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20WebflowConfigurer extends AbstractCasWebflowConfigurer {

    public OAuth20WebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                    final ConfigurableApplicationContext applicationContext,
                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val loginFlow = getLoginFlow();
        if (loginFlow != null) {
            val state = getTransitionableState(loginFlow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(createEvaluateAction("oauth20RegisteredServiceUIAction"));
            val createTicketState = getState(loginFlow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            createTicketState.getExitActionList().add(createEvaluateAction("oauth20SessionStoreTicketGrantingTicketAction"));
        }
    }
}
