package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

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
        final ActionState actionState = createActionState(flow, CasWebflowConstants.STATE_ID_CLIENT_ACTION, createEvaluateAction(CasWebflowConstants.STATE_ID_CLIENT_ACTION));
        final TransitionSet transitionSet = actionState.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW));
        setStartState(flow, actionState);
        registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
    }

    private void createStopWebflowViewState(final Flow flow) {
        final ViewState state = createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_PAC4J_STOP_WEBFLOW);
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                final Optional<ModelAndView> mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v)));
                return null;
            }
        });
    }
}
