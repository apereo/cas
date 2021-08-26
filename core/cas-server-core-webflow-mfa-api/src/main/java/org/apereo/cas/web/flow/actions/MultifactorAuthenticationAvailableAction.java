package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that will be called as part of the MFA subflow to determine if a MFA provider
 * is up and available to provide authentications.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class MultifactorAuthenticationAvailableAction extends AbstractMultifactorAuthenticationAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = WebUtils.getRegisteredService(requestContext);
        return provider.isAvailable(service) ? yes() : no();
    }
}
