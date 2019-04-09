package org.apereo.cas.web.flow.login;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SetServiceUnauthorizedRedirectUrlAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SetServiceUnauthorizedRedirectUrlAction extends AbstractAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getAccessStrategy() != null) {
            val unauthorizedRedirectUrl = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
            LOGGER.debug("Putting unauthorized redirect URL [{}] into the webflow", unauthorizedRedirectUrl);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext, unauthorizedRedirectUrl);
        }
        return null;
    }
}

