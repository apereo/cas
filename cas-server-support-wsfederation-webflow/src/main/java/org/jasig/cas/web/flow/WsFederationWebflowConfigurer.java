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

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("wsFederationWebflowConfigurer")
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String WS_FEDERATION_ACTION = "wsFederationAction";
    private static final String WS_FEDERATION_REDIRECT = "wsFederationRedirect";

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        
        final Expression expression = createExpression(flow, "flowScope.WsFederationIdentityProviderUrl", String.class);
        final ActionExecutingViewFactory viewFactory = new ActionExecutingViewFactory(
                new ExternalRedirectAction(expression));

        createEndState(flow, WS_FEDERATION_REDIRECT, viewFactory);
        final ActionState actionState = createActionState(flow, WS_FEDERATION_ACTION, createEvaluateAction(WS_FEDERATION_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        setStartState(flow, actionState);

        final TransitionableState initLoginState = flow.getTransitionableState(CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        for (final Transition transition : initLoginState.getTransitionSet()) {
            if (transition.getId().equals(CasWebflowConstants.TRANSITION_ID_GENERATED)) {
                final TargetStateResolver targetStateResolver = (TargetStateResolver) fromStringTo(TargetStateResolver.class)
                        .execute(WS_FEDERATION_REDIRECT);
                transition.setTargetStateResolver(targetStateResolver);
                break;
            }
        }
    }
}
