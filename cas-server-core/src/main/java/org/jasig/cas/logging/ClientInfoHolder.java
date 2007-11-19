/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging;

/**
 * Exposes some client specific information to the event handlers.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class ClientInfoHolder {
    
    private final static ThreadLocal<ClientInfo> THREAD_LOCAL = new ThreadLocal<ClientInfo>();
    
    public static ClientInfo getClientInfo() {
        return THREAD_LOCAL.get();
    }
    
    public static void setClientInfo(final ClientInfo clientInfo) {
        THREAD_LOCAL.set(clientInfo);
    }
    
    public static void clear() {
        THREAD_LOCAL.remove();
    }

}
