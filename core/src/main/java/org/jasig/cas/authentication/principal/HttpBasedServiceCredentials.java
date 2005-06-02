/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.net.URL;

/**
 * The Credentials representing an HTTP-based service. HTTP-based services (such
 * as web applications) are often represented by the URL entry point of the
 * application.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class HttpBasedServiceCredentials implements Credentials {

    /** Unique Serializable ID. */
    private static final long serialVersionUID = 3904681574350991665L;

    /** The callbackURL to check that identifies the application. */
    private final URL callbackUrl;

    /**
     * Constructor that takes the URL of the HTTP-based service and creates the
     * Credentials object.
     * 
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
