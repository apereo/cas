package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OAuth20WebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20WebflowConfigurer extends AbstractCasWebflowConfigurer {

    private Action oauth20RegisteredServiceUIAction;

    public void setOauth20RegisteredServiceUIAction(final Action oauth20RegisteredServiceUIAction) {
        this.oauth20RegisteredServiceUIAction = oauth20RegisteredServiceUIAction;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow loginFlow = getLoginFlow();
        if (loginFlow != null) {
            final ViewState state = (ViewState) loginFlow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(this.oauth20RegisteredServiceUIAction);
        }
    }
}
