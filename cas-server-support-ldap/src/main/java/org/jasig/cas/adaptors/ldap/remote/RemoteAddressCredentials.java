/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap.remote;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RemoteAddressCredentials implements Credentials {
    
    /**
     * Unique Id for serialization
     */
    private static final long serialVersionUID = -1219945780227815281L;
    
    private final String remoteAddress;
    
    public RemoteAddressCredentials(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    public String getRemoteAddress() {
        return this.remoteAddress;
    }
    
    public String toString() {
        return "[remote IP Address: " + this.remoteAddress + "]";
    }
}
