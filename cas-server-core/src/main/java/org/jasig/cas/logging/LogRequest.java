/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public final class LogRequest {
    
    private final ClientInfo clientInfo;
    
    private final String principal;
    
    private final String service;
    
    private final String eventType;
    
    public LogRequest(final ClientInfo clientInfo, final String principal, final String service, final String eventType) {
        this.clientInfo = clientInfo;
        this.principal = principal;
        this.service = service;
        this.eventType = eventType;
    }

    public ClientInfo getClientInfo() {
        return this.clientInfo;
    }

    public String getPrincipal() {
        return this.principal;
    }

    public String getService() {
        return this.service;
    }

    public String getEventType() {
        return this.eventType;
    }
}
