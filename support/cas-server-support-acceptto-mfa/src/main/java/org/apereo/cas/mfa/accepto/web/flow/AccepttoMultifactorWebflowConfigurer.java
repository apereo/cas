package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class AccepttoMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /**
     * Webflow event id.
     */
    public static final String MFA_ACCEPTTO_EVENT_ID = "mfa-acceptto";

    private final FlowDefinitionRegistry flowDefinitionRegistry;

    public AccepttoMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                final FlowDefinitionRegistry flowDefinitionRegistry,
                                                final ApplicationContext applicationContext,
                                                final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.flowDefinitionRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();

        if (flow != null) {
            registerMultifactorProviderAuthenticationWebflow(flow, MFA_ACCEPTTO_EVENT_ID,
                this.flowDefinitionRegistry, casProperties.getAuthn().getMfa().getAcceptto().getId());
            val startState = getStartState(flow);
            addActionsToActionStateExecutionListAt(flow, startState.getId(), 0,
                createEvaluateAction("mfaAccepttoMultifactorValidateChannelAction"));

            createTransitionForState(startState,
                CasWebflowConstants.TRANSITION_ID_FINALIZE, "accepttoFinalizeAuthentication");
            val finalizeAuthN = createActionState(flow, "accepttoFinalizeAuthentication",
                new AccepttoFinalizeAuthenticationAction());
            createTransitionForState(finalizeAuthN,
                CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        }

    }

    @Slf4j
    private static class AccepttoFinalizeAuthenticationAction extends AbstractAction {
        @Override
        protected Event doExecute(final RequestContext requestContext) {
            return new EventFactorySupport().success(this);
        }
    }
}
