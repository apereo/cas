package org.apereo.cas.web.flow.mfa;

import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that will be called as part of the MFA subflow to determine if a MFA provider
 * is up and available to provide authentications.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class MultifactorAuthenticationAvailableAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final MultifactorAuthenticationProvider provider = requestContext.getFlowScope().get("provider",
                MultifactorAuthenticationProvider.class);
        if (provider.isAvailable()) {
            return yes();
        }
        return no();
    }
}
