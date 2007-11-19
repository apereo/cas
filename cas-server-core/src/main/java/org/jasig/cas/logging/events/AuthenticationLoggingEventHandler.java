/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.events;

import org.jasig.cas.event.AuthenticationEvent;
import org.jasig.cas.logging.ClientInfo;
import org.jasig.cas.logging.LogRequest;
import org.jasig.cas.logging.LoggingManager;
import org.springframework.context.ApplicationEvent;

/**
 * Implementation of an EventHandler that can log an Authentication request.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class AuthenticationLoggingEventHandler extends AbstractLoggingEventHandler {
    
    public AuthenticationLoggingEventHandler(final LoggingManager loggingManager) {
        super(loggingManager);
    }

    protected LogRequest constructLogRequest(final ApplicationEvent event,
        final ClientInfo clientInfo) {
        final AuthenticationEvent authenticationEvent = (AuthenticationEvent) event;
        final String principal = authenticationEvent.getCredentials().toString();
        final String eventType = "AUTHENTICATION_" + (authenticationEvent.isSuccessfulAuthentication() ? "SUCCESS" : "FAILURE");
        final String service = null;
        return new LogRequest(clientInfo, principal, service, eventType);
    }

    public boolean supports(final ApplicationEvent event) {
        return event instanceof AuthenticationEvent;
    }
}
