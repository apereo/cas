/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.URL;

/**
 * The Credentials representing an HTTP-based service.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class HttpBasedServiceCredentials implements Credentials {

    private static final long serialVersionUID = 3904681574350991665L;

    final private URL callbackUrl;

    /**
     * @param callbackUrl the URL representing the service
     */
    public HttpBasedServiceCredentials(URL callbackUrl) {
        if (callbackUrl == null) {
            throw new IllegalArgumentException("callbackUrl must be set on " + this.getClass().getName());
        }
        this.callbackUrl = callbackUrl;
    }

    /**
     * @return Returns the callbackUrl.
     */
    public URL getCallbackUrl() {
        return this.callbackUrl;
    }
}