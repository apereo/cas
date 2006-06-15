/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;

import org.springframework.util.Assert;

/**
 * Class representing a service we have registered with the system.
 * RegisteredServices are assumed to be "approved" services and thus have
 * special options, such as allowing proxying, forcing renew=true, and the
 * ability to skin the login.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class RegisteredService {

    /** The unique String to identify this service. */
    private final String id;

    /** Is this service allowed to do proxying? */
    private final boolean allowedToProxy;

    /** Does this service always enforce renew = true. */
    private final boolean forceAuthentication;

    /** The theme name for this service. */
    private final String theme;

    /** The proxyUrl of the service if it needs one. */
    private final URL proxyUrl;

    /**
     * Constructs a new RegisteredService with the required property of id and
     * the optional theme and proxyUrl.
     * 
     * @param id The identifier for the service.
     * @param allowedToProxy Is this service allowed to proxy
     * @param forceAuthentication does it opt out of single sign on
     * @param theme the theme associated with the service
     * @param proxyUrl the proxyUrl of the service if applicable.
     * @throws IllegalArgumentException if the ID is null.
     */
    public RegisteredService(final String id, final boolean allowedToProxy,
        final boolean forceAuthentication, final String theme,
        final URL proxyUrl) {
        Assert.notNull(id, "id cannot be null");

        this.id = id;
        this.allowedToProxy = allowedToProxy;
        this.forceAuthentication = forceAuthentication;
        this.theme = theme;
        this.proxyUrl = proxyUrl;
    }

    /**
     * @return Returns the allowedToProxy.
     */
    public final boolean isAllowedToProxy() {
        return this.allowedToProxy;
    }

    /**
     * @return Returns the forceAuthentication.
     */
    public final boolean isForceAuthentication() {
        return this.forceAuthentication;
    }

    /**
     * @return Returns the id.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * @return Returns the theme.
     */
    public final String getTheme() {
        return this.theme;
    }

    /**
     * Method to retrieve the Proxy URL.
     * 
     * @return the Proxy URL or null if there is none.
     */
    public final URL getProxyUrl() {
        return this.proxyUrl;
    }
}
