package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * The {@link Pac4jWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class Pac4jWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Action saml2ClientLogoutAction;
    
    public Pac4jWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                  final Action saml2ClientLogoutAction, final ApplicationContext applicationContext,
                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        this.saml2ClientLogoutAction = saml2ClientLogoutAction;
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            createClientActionActionState(flow);
            createStopWebflowViewState(flow);
            createSaml2ClientLogoutAction();
        }
    }

    private void createSaml2ClientLogoutAction() {
        final Flow logoutFlow = getLogoutFlow();
        final DecisionState state = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        state.getEntryActionList().add(saml2ClientLogoutAction);
    }

    private void createClientActionActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, DelegatedClientAuthenticationAction.CLIENT_ACTION,
                createEvaluateAction(DelegatedClientAuthenticationAction.CLIENT_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
                CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition(DelegatedClientAuthenticationAction.STOP,
                DelegatedClientAuthenticationAction.STOP_WEBFLOW));
        setStartState(flow, actionState);
        registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
    }

    private void createStopWebflowViewState(final Flow flow) {
        final ViewState state = createViewState(flow, DelegatedClientAuthenticationAction.STOP_WEBFLOW,
                DelegatedClientAuthenticationAction.VIEW_ID_STOP_WEBFLOW);
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                final Optional<ModelAndView> mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request,
                        response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v)));
                return null;
            }
        });
    }
}
