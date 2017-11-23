package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
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
 * couldn't find entry for the certificate in an attribute repository and
 * falling back to principal from the certificate is turned off via:
 * {@code cas.authn.x509.principal.returnNull=true} provided by
 * {@link PersonDirectoryPrincipalResolverProperties#isReturnNull()}.
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

    private static final String EVENT_ID_START_X509 = "startX509Authenticate";

    public X509WebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                 final ApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState actionState = createActionState(flow, EVENT_ID_START_X509, createEvaluateAction("x509Check"));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                    CasWebflowConstants.TRANSITION_ID_WARN));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR,
                    CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                    CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM));

            actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
            registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);

            final ActionState state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, EVENT_ID_START_X509, true);
        }
    }
}
