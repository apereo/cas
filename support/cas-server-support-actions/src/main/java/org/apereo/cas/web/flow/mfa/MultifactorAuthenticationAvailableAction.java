package org.apereo.cas.web.flow.mfa;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
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
        final String flowId = requestContext.getActiveFlow().getId();
        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final MultifactorAuthenticationProvider provider =
                MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(flowId, applicationContext)
                .orElseThrow(AuthenticationException::new);
        if (provider.isAvailable()) {
            return yes();
        }
        return no();
    }
}
