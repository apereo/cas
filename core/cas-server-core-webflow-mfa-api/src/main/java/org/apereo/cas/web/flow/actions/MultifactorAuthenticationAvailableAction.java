package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@Slf4j
public class MultifactorAuthenticationAvailableAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val flowId = requestContext.getActiveFlow().getId();
        val applicationContext = ApplicationContextProvider.getApplicationContext();
        val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(flowId, applicationContext)
                .orElseThrow(AuthenticationException::new);
        if (provider.isAvailable()) {
            return yes();
        }
        return no();
    }
}
