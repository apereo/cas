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
    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The services manager with access to the registry. **/
    protected ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        logger.debug("Initialized login sequence");
        return success();
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
