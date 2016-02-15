package org.jasig.cas.web.flow;

import org.jasig.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Performs an authorization check for the gateway request if there is no Ticket Granting Ticket.
 *
 * @author Scott Battaglia
 * @since 3.4.5
 */
@Component("producingMessageAction")
public class producingMessageAction extends AbstractAction {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @NotNull
    private final ServicesManager servicesManager;

    /**
     * Initialize the component with an instance of the services manager.
     * @param servicesManager the service registry instance.
     */
    @Autowired
    public producingMessageAction(@Qualifier("servicesManager") final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        //Put the producer codes above        
        
    	
    	//If there are many next steps(e.g. generateServiceTicket), it's important what you return.
    	//This action state has only one next step.
    	//So, when return success, it just goes to the next step.
        return success();
    }
}
