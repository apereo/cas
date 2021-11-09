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
 * The {@link X509WebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for x509 integration.
 * <p>
 * Creates a flow that starts by trying to construct credentials using an X509
 * certificate found as request attribute with key javax.servlet.request.X509Certificate
 * {@link X509CertificateCredentialsNonInteractiveAction}.
 * If the check of the certificate is valid, flow goes to sendTicketGrantingTicket.
 * On error or authenticationFailure, the user is sent to the login page.
 * The authenticationFailure outcome can happen when CAS got a valid certificate but
 * couldn't find entry for the certificate in an attribute repository.
 * <p>
 * Credentials are cleared out at the end of the action in case the user
 * is sent to the login page where the X509 credentials object will cause
 * errors (e.g. no username property)
 * <p>
 * The X509 action is added to the main login flow by overriding the @link CasWebflowConstants#TRANSITION_ID_SUCCESS}
 * outcome of the {@link CasWebflowConstants#STATE_ID_INIT_LOGIN_FORM} action.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class X509WebflowConfigurer extends AbstractCasWebflowConfigurer {

    public X509WebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                 final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getX509().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_X509_START,
                CasWebflowConstants.ACTION_ID_X509_CHECK);
            val transitionSet = actionState.getTransitionSet();

            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.TRANSITION_ID_WARN));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStateIdOnX509Failure(flow)));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
                CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS));

            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));

            val initState = getState(flow, CasWebflowConstants.STATE_ID_AFTER_INIT_LOGIN_FORM, ActionState.class);
            createTransitionForState(initState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_X509_START, true);
        }
    }

    private String getStateIdOnX509Failure(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_AFTER_INIT_LOGIN_FORM, ActionState.class);
        return state.getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
    }
}
