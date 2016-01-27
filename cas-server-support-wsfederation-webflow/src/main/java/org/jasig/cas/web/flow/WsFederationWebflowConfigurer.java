package org.jasig.cas.web.flow;

import org.springframework.binding.expression.Expression;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;

import java.util.Iterator;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("wsFederationWebflowConfigurer")
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();


        final Expression expression = createExpression(flow, "flowScope.WsFederationIdentityProviderUrl", String.class);
        final ActionExecutingViewFactory viewFactory = new ActionExecutingViewFactory(
                new ExternalRedirectAction(expression));

        createEndState(flow, "wsFederationRedirect", viewFactory);
        final ActionState actionState = createActionState(flow, "wsFederationAction", createEvaluateAction("wsFederationAction"));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_ERROR, getStartState(flow).getId()));
        setStartState(flow, actionState);

        final TransitionableState loginTicketState = flow.getTransitionableState(STATE_ID_GENERATE_LOGIN_TICKET);
        final Iterator<Transition> it = loginTicketState.getTransitionSet().iterator();
        while (it.hasNext()) {
            final Transition transition = it.next();
            if (transition.getId().equals(TRANSITION_ID_GENERATED)) {
                final TargetStateResolver targetStateResolver = (TargetStateResolver) fromStringTo(TargetStateResolver.class)
                        .execute("wsFederationRedirect");
                transition.setTargetStateResolver(targetStateResolver);
                break;
            }
        }
    }
}
