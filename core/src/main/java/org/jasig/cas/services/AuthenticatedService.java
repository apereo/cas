/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.net.URL;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class AuthenticatedService {

    final private String id;

	final private boolean allowedToProxy;

	final private boolean forceAuthentication;

	final private String theme;

	final private SingleSignoutCallback singleSignoutCallback;
	
	final private URL proxyUrl;

    public AuthenticatedService(final String id, final boolean allowedToProxy,
        final boolean forceAuthentication, final String theme, final SingleSignoutCallback singleSignoutCallback, final URL proxyUrl) {
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

	public SingleSignoutCallback getSingleSignoutCallback() {
		return this.singleSignoutCallback;
	}

	public URL getProxyUrl() {
		return this.proxyUrl;
	}
}
