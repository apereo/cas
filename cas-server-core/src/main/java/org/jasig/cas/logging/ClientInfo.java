/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
@Embeddable
public class ClientInfo {

    @Column(name="CLIENT_IP_ADDRESS",length=15,nullable=false,updatable=false,insertable=true)
    private String clientIpAddress;
    
    @Column(name="CLIENT_USER_AGENT",length=255,nullable=false,updatable=false,insertable=true)
    private String userAgent;
    
    @Column(name="LOG_DATE",nullable=false,updatable=false,insertable=true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestDate = new Date();
    
    @Column(name="SERVER_IP_ADDRESS",nullable=false,updatable=false,insertable=true)
    private String serverIpAddress;

    public ClientInfo() {
        // nothing to do
    }
    
    public ClientInfo(final HttpServletRequest request) {
        this.clientIpAddress = request.getRemoteAddr();
        final String userAgentTemp = request.getHeader("User-Agent");
        
        if (userAgentTemp != null && userAgentTemp.length() > 255) {
            this.userAgent = userAgentTemp.substring(0, 255);
        } else {
            this.userAgent = userAgentTemp;
        }
        this.serverIpAddress = request.getLocalAddr();
    }
    
    public String getClientIpAddress() {
        return this.clientIpAddress;
    }
    
    public String getServerIpAddress() {
        return this.serverIpAddress;
    }
    
    public Date getRequestDate() {
        return this.requestDate;
    }
    
    public String getUserAgent() {
        return this.userAgent;
    }
}
