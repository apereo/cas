package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * The {@link DelegatedAuthenticationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class DelegatedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private final Action saml2ClientLogoutAction;

    public DelegatedAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                    final Action saml2ClientLogoutAction,
                                                    final ApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        this.saml2ClientLogoutAction = saml2ClientLogoutAction;
    }

    @Override
    protected void doInitialize() {
        final var flow = getLoginFlow();
        if (flow != null) {
            createClientActionActionState(flow);
            createStopWebflowViewState(flow);
            createSaml2ClientLogoutAction();
        }
    }

    private void createSaml2ClientLogoutAction() {
        final var logoutFlow = getLogoutFlow();
        final var state = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        state.getEntryActionList().add(saml2ClientLogoutAction);
    }

    private void createClientActionActionState(final Flow flow) {
        final var actionState = createActionState(flow, CasWebflowConstants.STATE_ID_CLIENT_ACTION, createEvaluateAction(CasWebflowConstants.STATE_ID_CLIENT_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW));
        setStartState(flow, actionState);
        registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
    }

    private void createStopWebflowViewState(final Flow flow) {
        final var state = createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_PAC4J_STOP_WEBFLOW);
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                final var mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v)));
                return null;
            }
        });
    }
}
