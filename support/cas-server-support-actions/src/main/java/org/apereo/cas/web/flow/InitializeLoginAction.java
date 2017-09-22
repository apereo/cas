package org.apereo.cas.web.flow;

import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitializeLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitializeLoginAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeLoginAction.class);

    /** The services manager with access to the registry. **/
    protected ServicesManager servicesManager;

    public InitializeLoginAction(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        LOGGER.debug("Initialized login sequence");
        return success();
    }
}
