package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
 * certificate found as request attribute with key {@code jakarta.servlet.request.X509Certificate}
 * {@link X509CertificateCredentialsNonInteractiveAction}.
 * If the check of the certificate is valid, flow goes to sendTicketGrantingTicket.
 * On error or authenticationFailure, the user is sent to the login page.
 * The authenticationFailure outcome can happen when CAS got a valid certificate but
 * couldn't find entry for the certificate in an attribute repository.
 * <p>
 * Credentials are cleared out at the end of the action in case the user
 * is sent to the login page where the X509 credentials object will cause
 * errors (e.g. no username property)
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class X509WebflowConfigurer extends AbstractCasWebflowConfigurer {

    public X509WebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                 final FlowDefinitionRegistry flowDefinitionRegistry,
                                 final ConfigurableApplicationContext applicationContext,
                                 final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getX509().getWebflow().getOrder());
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_X509_START,
                CasWebflowConstants.ACTION_ID_X509_CHECK);
            val transitionSet = actionState.getTransitionSet();

            val targetStates = getTargetStates(flow);

            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.TRANSITION_ID_WARN));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStateIdOnX509Failure(flow, targetStates)));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
            transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
                CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS));

            actionState.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));

            val initState = getState(flow, targetStates.getLeft(), ActionState.class);
            createTransitionForState(initState, targetStates.getRight(), CasWebflowConstants.STATE_ID_X509_START, true);
        }
    }

    private String getStateIdOnX509Failure(final Flow flow, final Pair<String, String> targetStates) {
        val state = getState(flow, targetStates.getLeft(), ActionState.class);
        return state.getTransition(targetStates.getRight()).getTargetStateId();
    }

    protected Pair<String, String> getTargetStates(final Flow flow) {
        val state = getState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, ActionState.class);
        if (state.getTransition(CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID) != null) {
            LOGGER.debug("Attaching X509 flow to passwordless flow");
            return new ImmutablePair<>(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID);
        }

        LOGGER.debug("Attaching X509 flow to regular login flow");
        return Pair.of(CasWebflowConstants.STATE_ID_AFTER_INIT_LOGIN_FORM, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }
}
