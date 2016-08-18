package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OAuth20LogoutWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20LogoutWebflowConfigurer extends AbstractCasWebflowConfigurer {
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLogoutFlow();
        final Action actionState = createEvaluateAction("oauth20LogoutAction");
        final ActionState s = (ActionState) flow.getState("doLogout");
        s.getEntryActionList().add(actionState);
    }
}
