package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OAuth20LogoutWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20LogoutWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private Action oauth20LogoutAction;
    private Action oauth20RegisteredServiceUIAction;

    public void setOauth20LogoutAction(final Action oauth20LogoutAction) {
        this.oauth20LogoutAction = oauth20LogoutAction;
    }

    public void setOauth20RegisteredServiceUIAction(final Action oauth20RegisteredServiceUIAction) {
        this.oauth20RegisteredServiceUIAction = oauth20RegisteredServiceUIAction;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLogoutFlow();

        if (flow != null) {
            final ActionState s = (ActionState) flow.getState(CasWebflowConstants.STATE_ID_DO_LOGOUT);
            s.getEntryActionList().add(oauth20LogoutAction);
        }

        final Flow loginFlow = getLoginFlow();
        if (loginFlow != null) {
            final ViewState state = (ViewState) loginFlow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(this.oauth20RegisteredServiceUIAction);
        }
    }
}
