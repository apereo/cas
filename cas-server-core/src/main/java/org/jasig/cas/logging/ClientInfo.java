/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class ClientInfo {

    private String clientIpAddress;
    
    private String userAgent;
    
    private Date requestDate = new Date();
    
    private String serverIpAddress;
    
    public ClientInfo(final HttpServletRequest request) {
        this.clientIpAddress = request.getRemoteAddr();
        this.userAgent = request.getHeader("User-Agent");
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
