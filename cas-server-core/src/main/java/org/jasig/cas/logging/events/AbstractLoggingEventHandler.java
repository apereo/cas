/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.events;

import org.jasig.cas.event.EventHandler;
import org.jasig.cas.logging.ClientInfo;
import org.jasig.cas.logging.ClientInfoHolder;
import org.jasig.cas.logging.LogRequest;
import org.jasig.cas.logging.LoggingManager;
import org.jasig.cas.util.annotation.NotNull;
import org.springframework.context.ApplicationEvent;

/**
 * Abstract EventHandler that can construct log requests and send them off to the logging manager.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public abstract class AbstractLoggingEventHandler implements EventHandler {
    
    @NotNull
    private final LoggingManager loggingManager;
    
    protected AbstractLoggingEventHandler(final LoggingManager loggingManager) {
        this.loggingManager = loggingManager;
    }

    public void handleEvent(final ApplicationEvent event) {
        final LogRequest logRequest = constructLogRequest(event, ClientInfoHolder.getClientInfo());
        
        this.loggingManager.log(logRequest);
    }

    /**
     * Construct the LogRequest based on the clientInfo and the supplied event.
     * 
     * @param event the event to log
     * @param clientInfo the basic client information
     * @return the fully constructed LogRequest
     */
    protected abstract LogRequest constructLogRequest(ApplicationEvent event, ClientInfo clientInfo);

}
