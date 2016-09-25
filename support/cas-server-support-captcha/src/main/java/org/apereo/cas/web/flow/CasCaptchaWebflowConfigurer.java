package org.apereo.cas.web.flow;

import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link CasCaptchaWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasCaptchaWebflowConfigurer extends AbstractCasWebflowConfigurer {
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState state = (ActionState) flow.getState(CasWebflowConstants.TRANSITION_ID_REAL_SUBMIT);
        final List<Action> currentActions = new ArrayList<>();
        state.getActionList().forEach(currentActions::add);
        currentActions.forEach(a -> state.getActionList().remove(a));
        
        state.getActionList().add(createEvaluateAction("validateCaptchaAction"));
        currentActions.forEach(a -> state.getActionList().add(a));
        
        state.getTransitionSet().add(createTransition("captchaError", CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }
}
