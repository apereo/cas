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


public abstract class AbstractLoggingEventHandler implements EventHandler {
    
    @NotNull
    private final LoggingManager loggingManager;
    
    protected AbstractLoggingEventHandler(final LoggingManager loggingManager) {
        this.loggingManager = loggingManager;
    }

    public void handleEvent(ApplicationEvent event) {
        final LogRequest logRequest = constructLogRequest(event, ClientInfoHolder.getClientInfo());
        
        this.loggingManager.log(logRequest);
    }

    protected abstract LogRequest constructLogRequest(ApplicationEvent event, ClientInfo clientInfo);

}
