package org.apereo.cas.web.flow;

import org.apereo.cas.support.pac4j.web.flow.ClientAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
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

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, ClientAction.CLIENT_ACTION,
                createEvaluateAction(ClientAction.CLIENT_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition(ClientAction.STOP, ClientAction.STOP_WEBFLOW));
        setStartState(flow, actionState);
        final ViewState state = createViewState(flow, ClientAction.STOP_WEBFLOW, ClientAction.VIEW_ID_STOP_WEBFLOW);
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) throws Exception {
                final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
                final HttpServletResponse response = WebUtils.getHttpServletResponse(requestContext);
                final Optional<ModelAndView> mv = ClientAction.hasDelegationRequestFailed(request, response.getStatus());
                if (mv.isPresent()) {
                    mv.get().getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v));
                }
                return null;
            }
        });
    }
}
