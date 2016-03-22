package org.jasig.cas.web.flow;

import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitializeLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("initializeLoginAction")
public class InitializeLoginAction extends AbstractAction {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /** The services manager with access to the registry. **/
    
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        logger.debug("Initialized login sequence");
        return success();
    }
}
