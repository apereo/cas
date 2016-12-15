package org.apereo.cas.web.flow;

import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
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

    public Pac4jWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ActionState actionState = createActionState(flow, DelegatedClientAuthenticationAction.CLIENT_ACTION,
                    createEvaluateAction(DelegatedClientAuthenticationAction.CLIENT_ACTION));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                    CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
            actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
            actionState.getTransitionSet().add(createTransition(DelegatedClientAuthenticationAction.STOP, DelegatedClientAuthenticationAction.STOP_WEBFLOW));
            setStartState(flow, actionState);
            final ViewState state = createViewState(flow, DelegatedClientAuthenticationAction.STOP_WEBFLOW, DelegatedClientAuthenticationAction.VIEW_ID_STOP_WEBFLOW);
            state.getEntryActionList().add(new AbstractAction() {
                @Override
                protected Event doExecute(final RequestContext requestContext) throws Exception {
                    final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
                    final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
                    final Optional<ModelAndView> mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                    if (mv.isPresent()) {
                        mv.get().getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v));
                    }
                    return null;
                }
            });
        }
    }
}
