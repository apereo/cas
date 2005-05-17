/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.http;

import java.io.IOException;
import java.net.URL;

/**
 * Based on code by Niels Campbell.
 * http://tinyurl.com/dccnc
 * 
 * @author Niels Campbell (niels_campbell_at_lycos.co.uk)
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class HttpTimeoutHandler extends sun.net.www.protocol.http.Handler {

    /** Timeout time. */
    private int iSoTimeout = 0;

    public HttpTimeoutHandler(int iSoTimeout) {
        // Divide the time out by two because two connection attempts are made
        // in HttpClient.parseHTTP()

        if (iSoTimeout % 2 != 0) {
            iSoTimeout++;
        }
        this.iSoTimeout = (iSoTimeout / 2);
    }

    protected java.net.URLConnection openConnection(final URL u)
        throws IOException {
        return new HttpTimeoutURLConnection(u, this, this.iSoTimeout);
    }

    protected String getProxy() {
        return this.proxy;
    }

    protected int getProxyPort() {
        return this.proxyPort;
    }
}
