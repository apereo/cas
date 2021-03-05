package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;

/**
 * This is {@link SamlIdPWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public SamlIdPWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(createEvaluateAction("samlIdPMetadataUIParserAction"));
            val createTicketState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
            createTicketState.getExitActionList().add(createEvaluateAction("samlIdPSessionStoreTicketGrantingTicketAction"));

            val h = new TransitionExecutingFlowExecutionExceptionHandler();
            h.add(SamlException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
            flow.getExceptionHandlerSet().add(h);
        }
    }
}
