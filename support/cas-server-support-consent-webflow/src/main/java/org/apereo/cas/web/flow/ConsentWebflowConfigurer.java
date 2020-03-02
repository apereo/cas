package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link ConsentWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConsentWebflowConfigurer extends AbstractCasWebflowConfigurer {
    static final String VIEW_ID_CONSENT_VIEW = "casConsentView";

    static final String STATE_ID_CONSENT_CONFIRM = "confirmAttributeConsent";

    private static final String ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT = "generateServiceTicketAfterConsent";

    public ConsentWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                    final ConfigurableApplicationContext applicationContext,
                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getConsent().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();

        if (flow != null) {
            createConsentRequiredCheckAction(flow);
            createConsentTransitions(flow);
            createConsentView(flow);
        }
    }

    private void createConsentView(final Flow flow) {
        val state = createViewState(flow, VIEW_ID_CONSENT_VIEW, VIEW_ID_CONSENT_VIEW);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CONFIRM, STATE_ID_CONSENT_CONFIRM);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_CANCEL, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

        val action = createActionState(flow, STATE_ID_CONSENT_CONFIRM, createEvaluateAction("confirmConsentAction"));
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT);
    }

    private void createConsentTransitions(final Flow flow) {
        val sendTicket = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        createTransitionForState(sendTicket, CheckConsentRequiredAction.EVENT_ID_CONSENT_REQUIRED, VIEW_ID_CONSENT_VIEW);
    }

    private void createConsentRequiredCheckAction(final Flow flow) {
        createEvaluateActionForExistingActionState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, "checkConsentRequiredAction");
        createClonedActionState(flow, ACTION_GEN_SERVICE_TICKET_AFTER_CONSENT, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
    }
}
