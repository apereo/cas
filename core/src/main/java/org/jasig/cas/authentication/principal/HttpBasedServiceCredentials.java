/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.URL;

/**
 * The Credentials representing an HTTP-based service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class HttpBasedServiceCredentials implements Credentials {

    /** Unique Serializable ID. */
    private static final long serialVersionUID = 3904681574350991665L;

    /** The callbackURL to check. */
    private final URL callbackUrl;

    /**
     * @param callbackUrl the URL representing the service
     * @throws IllegalArgumentException if the callbackUrl is null.
     */
    public HttpBasedServiceCredentials(final URL callbackUrl) {
        if (callbackUrl == null) {
            throw new IllegalArgumentException("callbackUrl must be set on "
                + this.getClass().getName());
        }
        this.callbackUrl = callbackUrl;
    }

    /**
     * @return Returns the callbackUrl.
     */
    public final URL getCallbackUrl() {
        return this.callbackUrl;
    }

    public final String toString() {
        return this.callbackUrl.toExternalForm();
    }
}
