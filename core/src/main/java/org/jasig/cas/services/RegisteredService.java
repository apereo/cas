/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RegisteredService {

    /** The unique String to identify this service. */
    private final String id;

    /** Is this service allowed to do proxying? */
    private final boolean allowedToProxy;

    /** Does this service always enforce renew = true. */
    private final boolean forceAuthentication;

    /** The theme name for this service. */
    private final String theme;

    /** The callback for single sign out. */
    private final SingleSignoutCallback singleSignoutCallback;

    /** The proxyUrl of the service if it needs one. */
    private final URL proxyUrl;

    public RegisteredService(final String id, final boolean allowedToProxy,
        final boolean forceAuthentication, final String theme,
        final SingleSignoutCallback singleSignoutCallback, final URL proxyUrl) {
        if (id == null) {
            throw new IllegalArgumentException("id is a required parameter.");
        }

        this.id = id;
        this.allowedToProxy = allowedToProxy;
        this.forceAuthentication = forceAuthentication;
        this.theme = theme;
        this.singleSignoutCallback = singleSignoutCallback;
        this.proxyUrl = proxyUrl;
    }

    /**
     * @return Returns the allowedToProxy.
     */
    public boolean isAllowedToProxy() {
        return this.allowedToProxy;
    }

    /**
     * @return Returns the forceAuthentication.
     */
    public boolean isForceAuthentication() {
        return this.forceAuthentication;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns the theme.
     */
    public String getTheme() {
        return this.theme;
    }

    /**
     * Method to retrieve the Callback for Single singout.
     * @return the Single Signout Callback or null.
     */
    public SingleSignoutCallback getSingleSignoutCallback() {
        return this.singleSignoutCallback;
    }

    /**
     * Method to retrieve the Proxy URL.
     * @return the Proxy URL or null if there is none.
     */
    public URL getProxyUrl() {
        return this.proxyUrl;
    }
}
