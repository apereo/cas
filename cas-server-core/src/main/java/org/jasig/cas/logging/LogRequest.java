/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Representation of the information that CAS wishes to log for further processing
 * and auditing.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
@Entity
@Table(name="cas_logging")
public final class LogRequest {
    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Embedded
    private ClientInfo clientInfo;
    
    @Column(name="PRINCIPAL",length=255,nullable=true,updatable=false,insertable=true)
    private String principal;
    
    @Column(name="SERVICE",length=255,nullable=true,updatable=false,insertable=true)
    private String service;
    
    @Column(name="EVENT_TYPE",length=255,nullable=false,updatable=false,insertable=true)
    private String eventType;
    
    public LogRequest() {
        // nothing to do
    }
    
    public LogRequest(final ClientInfo clientInfo, final String principal, final String service, final String eventType) {
        this.clientInfo = clientInfo;
        this.principal = principal;
        if (service != null && service.length() > 255) {
            this.service = service.substring(0, 255);
        } else {
            this.service = service;
        }
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
