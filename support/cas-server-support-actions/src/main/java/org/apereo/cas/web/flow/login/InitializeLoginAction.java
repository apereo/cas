package org.apereo.cas.web.flow.login;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitializeLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class InitializeLoginAction extends AbstractAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        LOGGER.trace("Initialized login sequence");
        return success();
    }

    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        return requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }
}
