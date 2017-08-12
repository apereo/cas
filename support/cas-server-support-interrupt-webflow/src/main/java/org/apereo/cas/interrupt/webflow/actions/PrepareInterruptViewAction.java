package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareInterruptViewAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class PrepareInterruptViewAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareInterruptViewAction.class);
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Service service = WebUtils.getService(requestContext);
        final RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        final InterruptResponse response = InterruptUtils.getInterruptFrom(requestContext);
        
        return null;
    }
}
